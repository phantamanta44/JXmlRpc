package xyz.phanta.jxmlrpc.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class XmlRpcStruct<T extends XmlRpcData> implements XmlRpcData {

    private final Map<String, T> entries = new HashMap<>();

    public XmlRpcStruct(Map<String, T> source) {
        entries.putAll(source);
    }

    public XmlRpcStruct(XmlRpcStruct<T> source) {
        entries.putAll(source.entries);
    }

    public XmlRpcStruct() {
        // NO-OP
    }

    public void put(String key, T value) {
        entries.put(key, value);
    }

    public T get(String key) {
        return entries.get(key);
    }

    public T computeIfAbsent(String key, Supplier<T> valueFactory) {
        return entries.computeIfAbsent(key, k -> valueFactory.get());
    }

    public Set<Map.Entry<String, T>> getEntries() {
        return entries.entrySet();
    }

    @Override
    public String serializeXmlRpc() {
        StringBuilder sb = new StringBuilder("<struct>");
        for (Map.Entry<String, T> entry : entries.entrySet()) {
            sb.append("<member><name>")
                    .append(entry.getKey())
                    .append("</name><value>")
                    .append(entry.getValue().serializeXmlRpc())
                    .append("</value></member>");
        }
        return sb.append("</struct>").toString();
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof XmlRpcStruct && entries.equals(((XmlRpcStruct)obj).entries);
    }

    @Override
    public String toString() {
        return "{" + entries.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", ")) + "}";
    }

}
