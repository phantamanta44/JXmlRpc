package xyz.phanta.jxmlrpc.error;

public abstract class XmlRpcException extends RuntimeException {

    public XmlRpcException(String message) {
        super(message);
    }

    public XmlRpcException(String message, Throwable cause) {
        super(message, cause);
    }

}
