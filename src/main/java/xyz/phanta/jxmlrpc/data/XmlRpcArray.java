package xyz.phanta.jxmlrpc.data;

import xyz.phanta.jxmlrpc.util.JoinableAccumulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XmlRpcArray<T extends XmlRpcData> implements XmlRpcData, Iterable<T> {

    private static final Collector<XmlRpcData, JoinableAccumulator<XmlRpcData>, XmlRpcArray<XmlRpcData>> COLLECTOR
            = Collector.of(JoinableAccumulator::create, JoinableAccumulator::add, JoinableAccumulator::join,
            acc -> {
                XmlRpcArray<XmlRpcData> arr = new XmlRpcArray<>();
                acc.iterate(arr::add);
                return arr;
            }, Collector.Characteristics.UNORDERED);

    @SafeVarargs
    public static <T extends XmlRpcData> XmlRpcArray<T> of(T... elements) {
        XmlRpcArray<T> arr = new XmlRpcArray<>();
        for (T element : elements) {
            arr.add(element);
        }
        return arr;
    }

    @SuppressWarnings("unchecked")
    public static <T extends XmlRpcData> Collector<T, ?, XmlRpcArray<T>> collect() {
        return (Collector)COLLECTOR;
    }

    private final List<T> elements = new ArrayList<>();

    public XmlRpcArray(Iterable<T> source) {
        for (T element : source) {
            elements.add(element);
        }
    }

    public XmlRpcArray() {
        // NO-OP
    }

    public void add(T element) {
        elements.add(element);
    }

    public void addAll(Collection<T> toAdd) {
        elements.addAll(toAdd);
    }

    public T get(int index) {
        return elements.get(index);
    }

    public void remove(T element) {
        elements.remove(element);
    }

    public void removeAt(int index) {
        elements.remove(index);
    }

    public int getSize() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public Stream<T> stream() {
        return elements.stream();
    }

    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }

    @Override
    public String serializeXmlRpc() {
        StringBuilder sb = new StringBuilder("<array><data>");
        for (XmlRpcData element : elements) {
            sb.append("<value>").append(element.serializeXmlRpc()).append("</value>");
        }
        return sb.append("</data></array>").toString();
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof XmlRpcArray && elements.equals(((XmlRpcArray)obj).elements);
    }

    @Override
    public String toString() {
        return "[" + elements.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }

}
