package xyz.phanta.jxmlrpc.data;

public class XmlRpcInt implements XmlRpcData {

    public static final XmlRpcInt ZERO = new XmlRpcInt(0);

    public final int value;

    public XmlRpcInt(int value) {
        this.value = value;
    }

    @Override
    public String serializeXmlRpc() {
        return "<i4>" + value + "</i4>";
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof XmlRpcInt && value == ((XmlRpcInt)obj).value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

}
