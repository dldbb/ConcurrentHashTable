package LockFree;

import ConcurrentHashTable.ConcurrentHashTable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Jun Wang
 * implementation of lock free concurrent hash map.
 */
public class LockFreeHashTable<K, V> implements ConcurrentHashTable<K, V> {

    private volatile List<Node<K, V>> buckets;
    private int numBuckets;
    private volatile AtomicInteger size;

    public LockFreeHashTable(int numBuckets) {
        this.size = new AtomicInteger(0);
        this.numBuckets = numBuckets;
        this.buckets = new ArrayList<>();

        //initialize the buckets with heads
        for (int i = 0; i < this.numBuckets; i++) {
            this.buckets.add(new Node<>());
        }
    }

    @Override
    public int hash(K key) {
        return Math.abs(key.hashCode() % this.numBuckets);
    }

    @Override
    public V put(K key, V value) {
        int hash = hash(key);
        return addNode(key, value, buckets.get(hash));
    }

    private V addNode(K key, V value, Node<K, V> head) {
        Node<K, V> node = new Node<>(key, value);
        // the different objects may have same value
        int priority = node.priority;

        while (true) {
            Node<K, V> pre = head;
            // get stamp of pre node's next pointer
            int stamp = pre.next.getStamp();
            Node<K, V> successor = pre.next.getReference();

            // find a right position based on priority of key
            while (successor != null && successor.priority <= priority) {

                // if exist, just overwrite the value of this node
                if (successor.key.equals(key)) {
                    while (true) {
                        int valueStamp = successor.value.getStamp();
                        V oldValue = successor.value.getReference();
                        // avoid ABA problem when overwrite value
                        if (successor.value.compareAndSet(oldValue, value, valueStamp, valueStamp + 1)) {
                            return oldValue;
                        }
                    }
                }

                pre = successor;
                stamp = pre.next.getStamp();
                successor = pre.next.getReference();
            }

            node.next.set(successor, 0);

            // if pre is not deleted and point to right successor
            if (!pre.isDeleted.get()) {
                // use stamped to avoid ABA
                if (pre.next.compareAndSet(successor, node, stamp, stamp + 1)) {
                    //update the size of hashMap
                    this.size.incrementAndGet();
                    return null;
                }
            }
        }
    }

    @Override
    public V get(K key) {
        int hash = hash(key);
        Node<K, V> find = buckets.get(hash).next.getReference();
        int priority = key.hashCode();

        while (find != null) {
            if (priority < find.priority) {
                return null;
            }

            if (find.key.equals(key)) {
                if (find.isDeleted.get()) {
                    return null;
                } else {
                    return find.value.getReference();
                }
            }

            find = find.next.getReference();
        }

        return null;
    }

    @Override
    public V remove(K key) {
        Node<K, V> head = buckets.get(hash(key));
        // the different objects may have same value
        int priority = key.hashCode();

        while (true) {
            Node<K, V> pre = head;
            int stamp = pre.next.getStamp();
            Node<K, V> successor = pre.next.getReference();

            // find a right position based on priority of key
            while (successor != null && successor.priority <= priority) {
                // if exist, remove the node and return the value
                if (successor.key.equals(key)) {
                    successor.isDeleted.set(true);
                    break;
                }

                pre = successor;
                stamp = pre.next.getStamp();
                successor = pre.next.getReference();
            }

            // if didn't find entry
            if (successor == null || successor.priority > priority) {
                return null;
            }

            if (!pre.isDeleted.get()) {
                if (pre.next.compareAndSet(successor, successor.next.getReference(), stamp, stamp + 1)) {
                    this.size.decrementAndGet();
                    return successor.value.getReference();
                }
            }

        }
    }

    @Override
    public boolean remove(K key, V value) {

        Node<K, V> head = buckets.get(hash(key));
        // the different objects may have same value
        int priority = key.hashCode();

        while (true) {
            Node<K, V> pre = head;
            int stamp = pre.next.getStamp();
            Node<K, V> successor = pre.next.getReference();

            // find a right position based on priority of key
            while (successor != null && successor.priority <= priority) {
                // if exist, remove the node and return the value
                if (successor.key.equals(key) && successor.value.getReference().equals(value)) {
                    successor.isDeleted.compareAndSet(false, true);
                    break;
                }

                pre = successor;
                stamp = pre.next.getStamp();
                successor = pre.next.getReference();
            }

            // if didn't find entry
            if (successor == null || successor.priority > priority) {
                return false;
            }

            if (!pre.isDeleted.get()) {
                if (pre.next.compareAndSet(successor, successor.next.getReference(), stamp, stamp + 1)) {
                    this.size.decrementAndGet();
                    return true;
                }
            }
        }
    }

    @Override
    public int size() {
        return this.size.get();
    }

    @Override
    public boolean isEmpty() {
        return size.get() == 0;
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        V value = get(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keySet = new HashSet<>();

        for (Node<K, V> bucket : this.buckets) {
            Node<K, V> iter = bucket.next.getReference();
            while (iter != null) {
                if (!iter.isDeleted.get()) {
                    keySet.add(iter.key);
                    iter = iter.next.getReference();
                }
            }
        }

        return keySet;
    }

    @Override
    public Collection<V> values() {
        Collection<V> values = new ArrayList<V>();

        for (Node<K, V> bucket : this.buckets) {
            Node<K, V> iter = bucket.next.getReference();
            while (iter != null) {
                if (!iter.isDeleted.get()) {
                    values.add(iter.value.getReference());
                    iter = iter.next.getReference();
                }
            }
        }

        return values;
    }

}
