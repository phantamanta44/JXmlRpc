package xyz.phanta.jxmlrpc.data;

public enum XmlRpcBool implements XmlRpcData {

    TRUE(true),
    FALSE(false);

    public static XmlRpcBool forValue(boolean value) {
        return value ? TRUE : FALSE;
    }

    public final boolean value;

    XmlRpcBool(boolean value) {
        this.value = value;
    }

    @Override
    public String serializeXmlRpc() {
        return value ? "<boolean>1</boolean>" : "<boolean>0</boolean>";
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

}
