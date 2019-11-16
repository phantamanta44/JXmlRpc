package xyz.phanta.jxmlrpc;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xyz.phanta.jxmlrpc.data.XmlRpcData;
import xyz.phanta.jxmlrpc.data.XmlRpcInt;
import xyz.phanta.jxmlrpc.data.XmlRpcString;
import xyz.phanta.jxmlrpc.data.XmlRpcStruct;
import xyz.phanta.jxmlrpc.error.XmlRpcInvokeException;
import xyz.phanta.jxmlrpc.error.XmlRpcParseException;
import xyz.phanta.jxmlrpc.util.XmlRpcUtils;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class XmlRpcServer {

    private final Object controller;
    private final ControllerMapping mapping;
    private final DocumentBuilder xmlParser;

    @Nullable
    private HttpServer server = null;

    public XmlRpcServer(Object controller) {
        this.controller = controller;
        this.mapping = ControllerMapping.computeMapping(controller.getClass());
        try {
            xmlParser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Failed to initialize XML parser!", e);
        }
    }

    public void serve() throws IOException {
        serve(new InetSocketAddress("0.0.0.0", 8080));
    }

    public void serve(InetSocketAddress address) throws IOException {
        serve(address, Executors.newSingleThreadExecutor());
    }

    public void serve(InetSocketAddress address, Executor executor) throws IOException {
        if (server != null) {
            throw new IllegalStateException("Server is already started!");
        }
        server = HttpServer.create(address, 0);
        server.createContext("/", req -> {
            String response;
            try {
                XmlRpcData result = processRequest(req);
                response = buildResponse(result != null ? result : XmlRpcInt.ZERO);
            } catch (Throwable e) {
                XmlRpcStruct<XmlRpcData> errResponse = new XmlRpcStruct<>();
                errResponse.put("faultCode", new XmlRpcInt(-1));
                errResponse.put("faultString", new XmlRpcString(e));
                response = buildFaultResponse(errResponse);
            }
            try (BufferedOutputStream res = new BufferedOutputStream(req.getResponseBody())) {
                byte[] responseData = response.getBytes(StandardCharsets.UTF_8);
                req.sendResponseHeaders(200, responseData.length);
                res.write(responseData);
            }
        });
        server.setExecutor(executor);
        server.start();
    }

    public InetSocketAddress getServerAddress() {
        if (server == null) {
            throw new IllegalStateException("Server is not started!");
        }
        return server.getAddress();
    }

    public void kill() {
        if (server == null) {
            throw new IllegalStateException("Server is not started!");
        }
        server.stop(0);
        server = null;
    }

    @Nullable
    private XmlRpcData processRequest(HttpExchange req) throws Throwable {
        Element reqXml;
        try (InputStream reqBody = req.getRequestBody()) {
            reqXml = xmlParser.parse(reqBody).getDocumentElement();
        } catch (SAXException e) {
            throw new XmlRpcParseException("Failed to parse XMLRPC request!", e);
        }
        // not the most vigorous defensive parsing ever, but parsing xml sucks so whatever
        String methodName = null;
        List<Node> reqParamNodes = new ArrayList<>();
        if (reqXml.getTagName().equals("methodCall")) {
            NodeList reqXmlChildren = reqXml.getChildNodes();
            for (int i = 0; i < reqXmlChildren.getLength(); i++) {
                Node reqXmlChild = reqXmlChildren.item(i);
                if (reqXmlChild instanceof Element) {
                    switch (((Element)reqXmlChild).getTagName()) {
                        case "methodName":
                            methodName = reqXmlChild.getTextContent().trim();
                            break;
                        case "params": {
                            NodeList paramsXmlChildren = reqXmlChild.getChildNodes();
                            for (int j = 0; j < paramsXmlChildren.getLength(); j++) {
                                Node paramXml = paramsXmlChildren.item(j);
                                if (paramXml instanceof Element && ((Element)paramXml).getTagName().equals("param")) {
                                    NodeList paramXmlChildren = paramXml.getChildNodes();
                                    for (int k = 0; k < paramXmlChildren.getLength(); k++) {
                                        Node valueXml = paramXmlChildren.item(k);
                                        if (valueXml instanceof Element && ((Element)valueXml).getTagName().equals("value")) {
                                            reqParamNodes.add(XmlRpcUtils.extractNontrivialChild(valueXml));
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        if (methodName == null) {
            throw new XmlRpcParseException("Badly-formed XMLRPC request!");
        }
        XmlRpcData[] params = new XmlRpcData[reqParamNodes.size()];
        for (int i = 0; i < params.length; i++) {
            params[i] = XmlRpcData.parse(reqParamNodes.get(i));
        }
        return mapping.invoke(controller, methodName, params);
    }

    private static String buildResponse(XmlRpcData data) {
        return "<methodResponse><params><param><value>" + data.serializeXmlRpc() + "</value></param></params></methodResponse>";
    }

    private static String buildFaultResponse(XmlRpcData data) {
        return "<methodResponse><fault><value>" + data.serializeXmlRpc() + "</value></fault></methodResponse>";
    }

    private static class ControllerMapping {

        private static final Map<Class<?>, ControllerMapping> mappingCache = new HashMap<>();

        public static ControllerMapping computeMapping(Class<?> type) {
            return mappingCache.computeIfAbsent(type, ControllerMapping::new);
        }

        private final Map<String, Method> methodMap = new HashMap<>();

        private ControllerMapping(Class<?> type) {
            for (Method method : type.getDeclaredMethods()) {
                XmlRpcRoutine annot = method.getAnnotation(XmlRpcRoutine.class);
                if (annot != null) {
                    String routineName = annot.name();
                    if (routineName.isEmpty()) {
                        routineName = method.getName();
                    }
                    method.setAccessible(true);
                    methodMap.put(routineName, method);
                }
            }
        }

        @Nullable
        XmlRpcData invoke(Object controller, String methodName, XmlRpcData[] params) throws Throwable {
            Method method = methodMap.get(methodName);
            if (method == null) {
                throw new XmlRpcInvokeException("Could not find matching RPC method: " + methodName);
            }
            try {
                //noinspection RedundantCast
                return (XmlRpcData)method.invoke(controller, (Object[])params);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cached method in bad state!", e);
            } catch (IllegalArgumentException e) {
                throw new XmlRpcInvokeException("Bad arguments for RPC invocation!", e);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

    }

}
