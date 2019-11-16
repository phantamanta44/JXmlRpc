package xyz.phanta.jxmlrpc.data;

import java.time.Instant;

public class XmlRpcDate implements XmlRpcData {

    public final Instant time;

    public XmlRpcDate(Instant time) {
        this.time = time;
    }

    @Override
    public String serializeXmlRpc() {
        return "<dateTime.iso8601>" + time.toString() + "</dateTime.iso8601>";
    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof XmlRpcDate && time.equals(((XmlRpcDate)obj).time);
    }

    @Override
    public String toString() {
        return time.toString();
    }

}
