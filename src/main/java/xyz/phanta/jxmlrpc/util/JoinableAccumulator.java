package xyz.phanta.jxmlrpc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface JoinableAccumulator<T> {

    static <T> JoinableAccumulator<T> create() {
        return new Node<>();
    }

    void add(T element);

    default JoinableAccumulator<T> join(JoinableAccumulator<T> other) {
        return new ParentNode<>(this, other);
    }

    void iterate(Consumer<T> visitor);

    class Node<T> implements JoinableAccumulator<T> {

        private final List<T> elements = new ArrayList<>();

        Node() {
            // NO-OP
        }

        @Override
        public void add(T element) {
            elements.add(element);
        }

        @Override
        public void iterate(Consumer<T> visitor) {
            elements.forEach(visitor);
        }

    }

    class ParentNode<T> extends Node<T> {

        private final JoinableAccumulator<T> left, right;

        ParentNode(JoinableAccumulator<T> left, JoinableAccumulator<T> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public void iterate(Consumer<T> visitor) {
            left.iterate(visitor);
            right.iterate(visitor);
            super.iterate(visitor);
        }

    }

}
