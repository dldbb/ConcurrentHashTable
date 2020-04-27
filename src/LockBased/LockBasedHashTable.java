package LockBased;

import java.util.*;
import ConcurrentHashTable.ConcurrentHashTable;

public class LockBasedHashTable<K, V> implements ConcurrentHashTable<K, V> {

    protected static final class Segment {
        protected int count = 0;

        protected synchronized int getCount() {
            return this.count;
        }
        protected synchronized void synch(){}
    }

    static class Entry<K, V> implements Map.Entry<K, V> {
        protected final K key;
        protected volatile V value;
        protected final int hash;
        protected final Entry<K, V> next;

        Entry(int hash, K key, V value, Entry next) {
            this.value = value;
            this.hash = hash;
            this.key = key;
            this.next = next;
        }

        public Entry<K, V> getNext() {
            return next;
        }

        public int getHash() {
            return hash;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }

    public final Segment[] segments = new Segment[32];
    protected transient Entry<K, V>[] table;
    private static final int INITIAL_CAPACITY = 1 << 5;//32
    private static final double loadFactor = 0.75;
    private int numBuckets;

    public LockBasedHashTable() {
        this.table = newTable(INITIAL_CAPACITY, loadFactor);
        for (int i = 0; i < segments.length; i++) {
            segments[i] = new Segment();
        }
        numBuckets = INITIAL_CAPACITY;
    }

    protected Entry<K, V>[] newTable(int capacity, double loadFactor) {
            return (Entry<K, V>[])new Entry[capacity];
    }

    @Override
    public int hash(K key) {
        return Math.abs(key.hashCode() % this.numBuckets);
    }

    @Override
    public V put(final K key, final V value){
        int hashVal = hash(key);
        Segment seg = segments[(hashVal & 0x1F)];
        synchronized (seg) {
            int index = hashVal & table.length - 1;
            Entry<K, V> first = table[index];
            for (Entry<K, V> e = first; e != null; e = e.next) {
                if ((e.hash == hashVal) && (key.equals(e.key))) {
                    V oldValue = e.value;
                    e.value = value;
                    return oldValue;
                }
            }
            Entry<K, V> newEntry = new Entry(hashVal, key, value, first);
            table[index] = newEntry;
            seg.count += 1;
            return null;
        }
    }

    @Override
    public V get(final K key){
        int hashVal = hash(key);
        int index = hashVal & table.length - 1;
        Entry<K, V> first = table[index];
        for (Entry<K, V> e = first; e != null; e = e.next) {
            if ((e.hash == hashVal) && (key.equals(e.key))) {
                V value = e.value;
                if (value == null) {
                    break;
                }
                return value;
            }
        }
        return null;
    }

    @Override
    public V remove(K key) {
        int hashVal = hash(key);
        Segment seg = segments[(hashVal & 0x1F)];
        synchronized (seg) {
            int index = hashVal & table.length - 1;
            Entry<K, V> first = table[index];
            Entry<K, V> e = first;
            while (true) {
                if (e == null) {
                    return null;
                }
                if ((e.hash == hashVal) && (key.equals(e.key))) {
                    break;
                }
                e = e.next;
            }
            V oldValue = e.value;
            Entry<K, V> head = e.next;
            for (Entry<K, V> p = first; p != e; p = p.next) {
                head = new Entry<K, V>(p.hash, p.key, p.value, head);
            }
            table[index] = head;
            seg.count -= 1;
            return oldValue;
        }
    }

    @Override
    public boolean remove(K key, V value){
        int hashVal = hash(key);
        Segment seg = segments[(hashVal & 0x1F)];
        synchronized (seg) {
            int index = hashVal & table.length - 1;
            Entry<K, V> first = table[index];
            Entry<K, V> e = first;
            while (true) {
                if (e == null) {
                    return false;
                }
                if ((e.hash == hashVal) && (key.equals(e.key)))  {
                    if (e.value == value) {
                        break;
                    }else {
                        return false;
                    }
                }
                e = e.next;

            }
            //V oldValue = e.value;
            Entry<K, V> head = e.next;
            for (Entry<K, V> p = first; p != e; p = p.next) {
                head = new Entry<K, V>(p.hash, p.key, p.value, head);
            }
            table[index] = head;
            seg.count -= 1;
            return true;
        }
    }

    @Override
    public int size(){
        int c = 0;
        for (int i = 0; i < segments.length; i++) {
            c += segments[i].count;
        }
        return c;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(K key){
        return get(key) != null;
    }

    @Override
    public V getOrDefault(K key, V defaultValue){
        V value = get(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public Set<K> keySet(){
        Set<K> set = new HashSet<>();
        for (Entry<K, V> e : this.table) {
            Entry<K, V> iter = e;
            while (iter != null) {
                set.add(iter.key);
                iter = iter.next;
            }
        }
        return set;
    }

    @Override
    public Collection<V> values(){
        Collection<V> collection = new ArrayList<V>();
        for (Entry<K, V> e : this.table) {
            Entry<K, V> iter = e;
            while (iter != null) {
                collection.add(iter.value);
                iter = iter.next;
            }
        }
        return collection;
    }
}
