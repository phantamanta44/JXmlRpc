package xyz.phanta.jxmlrpc;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xyz.phanta.jxmlrpc.data.XmlRpcData;
import xyz.phanta.jxmlrpc.data.XmlRpcInt;
import xyz.phanta.jxmlrpc.data.XmlRpcString;
import xyz.phanta.jxmlrpc.data.XmlRpcStruct;
import xyz.phanta.jxmlrpc.error.XmlRpcFaultException;
import xyz.phanta.jxmlrpc.error.XmlRpcParseException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class XmlRpcClient {

    private final URI serverUri;
    private final DocumentBuilder xmlParser;

    public XmlRpcClient(URI serverUri) {
        this.serverUri = serverUri;
        try {
            xmlParser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Failed to initialize XML parser!", e);
        }
    }

    public URI getServerUri() {
        return serverUri;
    }

    public XmlRpcData invokeRemote(String methodName, XmlRpcData... params) throws IOException {
        HttpURLConnection conn = (HttpURLConnection)serverUri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setDoOutput(true);
        try (OutputStream streamOut = conn.getOutputStream()) {
            streamOut.write(buildRequest(methodName, params).getBytes(StandardCharsets.UTF_8));
        }
        Element resXml;
        try (InputStream streamIn = conn.getInputStream()) {
            if (conn.getResponseCode() >= 400) {
                throw new IOException(String.format("Http %d: %s", conn.getResponseCode(), conn.getResponseMessage()));
            }
            resXml = xmlParser.parse(streamIn).getDocumentElement();
        } catch (SAXException e) {
            throw new XmlRpcParseException("Failed to parse XMLRPC response!", e);
        }
        // not the most vigorous defensive parsing ever, but parsing xml sucks so whatever
        Node resultNode = null;
        boolean fault = false;
        if (resXml.getTagName().equals("methodResponse")) {
            NodeList resXmlChildren = resXml.getChildNodes();
            for (int i = 0; i < resXmlChildren.getLength(); i++) {
                Node paramsXml = resXmlChildren.item(i);
                Node valueContXml = null;
                fault = false;
                if (paramsXml instanceof Element) {
                    if (((Element)paramsXml).getTagName().equals("fault")) {
                        valueContXml = paramsXml;
                        fault = true;
                    } else if (((Element)paramsXml).getTagName().equals("params")) {
                        NodeList paramsXmlChildren = paramsXml.getChildNodes();
                        for (int j = 0; j < paramsXmlChildren.getLength(); j++) {
                            Node paramXml = paramsXmlChildren.item(j);
                            if (paramXml instanceof Element && ((Element)paramXml).getTagName().equals("param")) {
                                valueContXml = paramXml;
                                break;
                            }
                        }
                    }
                    if (valueContXml != null) {
                        resultNode = unwrapValueContainer(valueContXml.getChildNodes());
                        break;
                    }
                }
            }
        }
        if (resultNode == null) {
            throw new XmlRpcParseException("Badly-formed XMLRPC response!");
        }
        if (fault) {
            XmlRpcStruct<?> faultData = (XmlRpcStruct<?>)XmlRpcData.parse(resultNode);
            throw new XmlRpcFaultException(
                    ((XmlRpcInt)faultData.get("faultCode")).value,
                    ((XmlRpcString)faultData.get("faultString")).value);
        } else {
            return XmlRpcData.parse(resultNode);
        }
    }

    private static String buildRequest(String methodName, XmlRpcData[] params) {
        StringBuilder sb = new StringBuilder("<methodCall><methodName>")
                .append(methodName)
                .append("</methodName><params>");
        for (XmlRpcData param : params) {
            sb.append("<param><value>").append(param.serializeXmlRpc()).append("</value></param>");
        }
        return sb.append("</params></methodCall>").toString();
    }

    @Nullable
    private static Node unwrapValueContainer(NodeList valueContChildren) {
        for (int i = 0; i < valueContChildren.getLength(); i++) {
            Node valueXml = valueContChildren.item(i);
            if (valueXml instanceof Element && ((Element)valueXml).getTagName().equals("value")) {
                return valueXml.getFirstChild();
            }
        }
        return null;
    }

}
