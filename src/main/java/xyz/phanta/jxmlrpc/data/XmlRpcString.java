package xyz.phanta.jxmlrpc.data;

public class XmlRpcString implements XmlRpcData {

    public final String value;

    public XmlRpcString(String value) {
        this.value = value;
    }

    public XmlRpcString(Object value) {
        this(value.toString());
    }

    @Override
    public String serializeXmlRpc() {
        return "<string>" + value + "</string>";
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof XmlRpcString && value.equals(((XmlRpcString)obj).value);
    }

    @Override
    public String toString() {
        return value;
    }

}
