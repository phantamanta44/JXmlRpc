package xyz.phanta.jxmlrpc.data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class XmlRpcBase64 implements XmlRpcData {

    public static XmlRpcBase64 forUtf8(String str) {
        return forString(str, StandardCharsets.UTF_8);
    }

    public static XmlRpcBase64 forString(String str, Charset charset) {
        return forBytes(str.getBytes(charset));
    }

    public static XmlRpcBase64 forBytes(byte[] data) {
        return new XmlRpcBase64(Base64.getEncoder().encodeToString(data));
    }

    public final String data;

    public XmlRpcBase64(String data) {
        this.data = data;
    }

    public String decodeUtf8() {
        return decodeString(StandardCharsets.UTF_8);
    }

    public String decodeString(Charset charset) {
        return new String(decodeBytes(), charset);
    }

    public byte[] decodeBytes() {
        return Base64.getDecoder().decode(data);
    }

    @Override
    public String serializeXmlRpc() {
        return "<base64>" + data + "</base64>";
    }

    @Override
    public int hashCode() {
        return ~data.hashCode(); // makes this distinct from xmlrpc string hash code
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof XmlRpcBase64 && data.equals(((XmlRpcBase64)obj).data);
    }

    @Override
    public String toString() {
        return "b64:" + data;
    }

}
