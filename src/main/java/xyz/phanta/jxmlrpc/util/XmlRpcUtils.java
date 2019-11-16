package xyz.phanta.jxmlrpc.util;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlRpcUtils {

    public static Node extractNontrivialChild(Node parent) {
        NodeList children = parent.getChildNodes();
        for (int k = 0; k < children.getLength(); k++) {
            Node child = children.item(k);
            if (child instanceof Element) {
                return child;
            }
        }
        return parent.getFirstChild();
    }

}
