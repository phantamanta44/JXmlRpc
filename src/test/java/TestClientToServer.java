import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.phanta.jxmlrpc.XmlRpcClient;
import xyz.phanta.jxmlrpc.XmlRpcRoutine;
import xyz.phanta.jxmlrpc.XmlRpcServer;
import xyz.phanta.jxmlrpc.data.XmlRpcInt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

public class TestClientToServer {

    @Test
    public void testMultiplier() throws IOException {
        XmlRpcServer server = new XmlRpcServer(new Object() {
            @XmlRpcRoutine
            public XmlRpcInt multiply(XmlRpcInt a, XmlRpcInt b) {
                return new XmlRpcInt(a.value * b.value);
            }
        });
        server.serve();

        XmlRpcClient client = new XmlRpcClient(getAddressUri(server.getServerAddress()));
        XmlRpcInt result = (XmlRpcInt)client.invokeRemote("multiply", new XmlRpcInt(6), new XmlRpcInt(4));
        Assertions.assertEquals(24, result.value);

        server.kill();
    }

    private static URI getAddressUri(InetSocketAddress address) {
        String hostStr = address.getHostString();
        if (hostStr.contains(":")) { // deal with ipv6
            hostStr = "[" + hostStr + "]";
        }
        return URI.create("http://" + hostStr + ":" + address.getPort());
    }

}
