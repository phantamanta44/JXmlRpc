package xyz.phanta.jxmlrpc.data;

public class XmlRpcDouble implements XmlRpcData {

    public static final XmlRpcDouble ZERO = new XmlRpcDouble(0D);

    public final double value;

    public XmlRpcDouble(double value) {
        this.value = value;
    }

    @Override
    public String serializeXmlRpc() {
        return "<double>" + value + "</double>";
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof XmlRpcDouble && value == ((XmlRpcDouble)obj).value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

}
