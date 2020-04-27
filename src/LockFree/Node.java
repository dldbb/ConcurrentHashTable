package LockFree;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Jun Wang
 * Entry node for hash map
 */
public class Node<K, V> {

    public AtomicStampedReference<V> value; // use stamped to avoid ABA when overwrite the value in map
    public K key;
    public AtomicStampedReference<Node<K, V>> next; // use stamped to avoid ABA
    public AtomicBoolean isDeleted;
    public Integer priority;

    public Node() {
        this.value = null;
        this.key = null;
        next = new AtomicStampedReference<>(null, 0);
        isDeleted = new AtomicBoolean(false);
    }

    public Node(K key, V value) {
        this.value = new AtomicStampedReference<>(value, 0);
        this.key = key;
        next = new AtomicStampedReference<>(null, 0);
        isDeleted = new AtomicBoolean(false);
        // the different object may have same value, thus we also has to compare value
        priority = key.hashCode();
    }
}
