package xyz.phanta.jxmlrpc.error;

public class XmlRpcFaultException extends XmlRpcException {

    private final int faultCode;
    private final String faultMessage;

    public XmlRpcFaultException(int faultCode, String faultMessage) {
        super("XMLRPC fault " + faultCode + ": " + faultMessage);
        this.faultCode = faultCode;
        this.faultMessage = faultMessage;
    }

    public int getFaultCode() {
        return faultCode;
    }

    public String getFaultMessage() {
        return faultMessage;
    }

}
