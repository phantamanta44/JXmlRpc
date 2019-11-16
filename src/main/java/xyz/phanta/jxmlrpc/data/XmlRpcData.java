package xyz.phanta.jxmlrpc.data;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.phanta.jxmlrpc.error.XmlRpcParseException;
import xyz.phanta.jxmlrpc.util.XmlRpcUtils;

import java.time.Instant;

public interface XmlRpcData {

    String serializeXmlRpc();

    static XmlRpcData parse(Node xml) {
        if (xml instanceof Element) {
            switch (((Element)xml).getTagName()) {
                case "array": {
                    NodeList xmlChildren = xml.getChildNodes();
                    for (int i = 0; i < xmlChildren.getLength(); i++) {
                        Node dataXml = xmlChildren.item(i);
                        if (dataXml instanceof Element && ((Element)dataXml).getTagName().equals("data")) {
                            XmlRpcArray<XmlRpcData> arr = new XmlRpcArray<>();
                            NodeList dataXmlChildren = dataXml.getChildNodes();
                            for (int j = 0; j < dataXmlChildren.getLength(); j++) {
                                Node valueXml = dataXmlChildren.item(j);
                                if (valueXml instanceof Element && ((Element)valueXml).getTagName().equals("value")) {
                                    arr.add(parse(XmlRpcUtils.extractNontrivialChild(valueXml)));
                                }
                            }
                            return arr;
                        }
                    }
                    throw new XmlRpcParseException("Malformed XMLRPC array!");
                }
                case "base64":
                    return new XmlRpcBase64(xml.getTextContent());
                case "boolean":
                    return XmlRpcBool.forValue(!xml.getTextContent().equals("0"));
                case "dateTime.iso8601":
                    return new XmlRpcDate(Instant.parse(xml.getTextContent()));
                case "double":
                    return new XmlRpcDouble(Double.parseDouble(xml.getTextContent()));
                case "int":
                case "i4":
                    return new XmlRpcInt(Integer.parseInt(xml.getTextContent()));
                case "string":
                    return new XmlRpcString(xml.getTextContent());
                case "struct": {
                    XmlRpcStruct<XmlRpcData> struct = new XmlRpcStruct<>();
                    NodeList xmlChildren = xml.getChildNodes();
                    for (int i = 0; i < xmlChildren.getLength(); i++) {
                        Node memberXml = xmlChildren.item(i);
                        if (memberXml instanceof Element && ((Element)memberXml).getTagName().equals("member")) {
                            String name = null;
                            Node value = null;
                            NodeList memberXmlChildren = memberXml.getChildNodes();
                            for (int j = 0; j < memberXmlChildren.getLength(); j++) {
                                Node propXml = memberXmlChildren.item(j);
                                if (propXml instanceof Element) {
                                    switch (((Element)propXml).getTagName()) {
                                        case "name":
                                            name = propXml.getTextContent().trim();
                                            break;
                                        case "value":
                                            value = XmlRpcUtils.extractNontrivialChild(propXml);
                                            break;
                                    }
                                }
                            }
                            if (name != null && value != null) {
                                struct.put(name, parse(value));
                            }
                        }
                    }
                    return struct;
                }
                default:
                    throw new XmlRpcParseException("Bad XMLRPC type: " + ((Element)xml).getTagName());
            }
        } else { // must be implicit xmlrpc string
            return new XmlRpcString(xml.getTextContent());
        }
    }

}
