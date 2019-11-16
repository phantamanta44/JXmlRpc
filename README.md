# JXmlRpc

A very simple XMLRPC client/server implementation that only depends on Java's standard library.

## Usage

### XMLRPC-Encoded Data

JXmlRpc ships with implementations of all XMLRPC-encodable data types, which can be found in the `xyz.phanta.jxmlrpc.data` package.
Simple data types generally expose a constructor that wraps its argument (with the exception of booleans, which use an enum), as well as a public final field exposing the wrapped value.
More complex data types, such as collection types, expose various differing methods of construction for the sake of convenience.
For example, the `XmlRpcArray.collect()` method can be used to obtain a stream collector for producing XMLRPC-encoded arrays.
You are encouraged to read the source code for such data types if you wish to learn more about the available construction methods.

### Server

To use the XMLRPC server, construct an instance of `XmlRpcServer` and pass it some controller object.
A controller object is an object with one or more methods annotated with `@XmlRpcRoutine`.
These methods should satisfy the following properties:

* Their parameters, if any, must be XMLRPC-encoded (i.e. extending `XmlRpcData`) objects.
* They must always return an XMLRPC-encoded result object.
* They must be non-static methods.

A simple XMLRPC server with a routine for multiplying integers is presented below:

```java
public class MultiplyServer {

    public static void main(String[] args) throws IOException {
        XmlRpcServer server = new XmlRpcServer(new MultiplyServer());
        server.serve();
    }

    @XmlRpcRoutine
    public XmlRpcInt multiply(XmlRpcInt a, XmlRpcInt b) {
        return new XmlRpcInt(a.value * b.value);
    }

}
```

### Client

To use the XMLRPC client, construct an instance of `XmlRpcClient` and pass it a URI pointing to the XMLRPC server you want to connect to.
To make a remote procedure call, use `invokeRemote`, passing it the name of the method and the XMLRPC-encoded parameters you want to send.
In the case of a fault, an `XmlRpcFaultException` will be thrown.

A simple XMLRPC client use case is presented below, which interacts with the multiplication routine shown above:

```java
public class MultiplyClient {

    private final XmlRpcClient client;

    public MultiplyClient(URI serverUri) {
        client = new XmlRpcClient(serverUri);
    }

    public int multiply(int a, int b) {
        XmlRpcInt result = (XmlRpcInt)client.invokeRemote("multiply",
                new XmlRpcInt(a), new XmlRpcInt(b));
        return result.value;
    }

}
```

## Known Issues

* Does not handle malformed responses very well.
* The semantics of XMLRPC exception types are not very well-defined.
* Having to construct and unwrap XMLRPC-encoded data types all the time is a little clunky.
