package LockBased;

import java.util.*;
import ConcurrentHashTable.ConcurrentHashTable;

public class CuckooLockBasedHashTable<K, V> implements ConcurrentHashTable<K, V> {
    private int MAXN = 3000;
    private int ver = 5;

    static class Entry<K, V> implements Map.Entry<K, V> {
        protected final K key;
        protected volatile V value;
        protected final int hash;

        Entry(int hash, K key, V value) {
            this.value = value;
            this.hash = hash;
            this.key = key;
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

    protected transient Entry<K, V>[][] table;
    private Random r = new Random();
    private int[] counts = new int[ver];

    public CuckooLockBasedHashTable(int size) {
        MAXN = size / ver;// / 3 + 1;
        table = (Entry<K, V>[][])new Entry[ver][MAXN];
        for (int i = 0; i < ver; i++) {
            counts[i] = 0;
        }
    }

    @Override
    public int hash(K key) {
        return Math.abs(key.hashCode() % this.MAXN);
    }

    public int cuckooHash(int function, K key) {
        int num = key.hashCode();
        for (int i = 0; i < function; i++) {
            num /= MAXN;
            num++;
        }
        return Math.abs(num % (MAXN));
    }

    //@Override
    public int[] findPos(K key) {
        for (int i = 0; i < ver; i++) {
            int hashVal = cuckooHash(i, key);
            int index = hashVal % MAXN;
            if (table[i][index] != null && table[i][index].key.equals(key)) {
                int[] found = new int[2];
                found[0] = i;
                found[1] = index;
                return found;
            }
        }
        return null;
    }

    public V replace(int[] pos, K key, V value) {
        if (pos == null) {
            return null;
        }
        V oldValue = table[pos[0]][pos[1]].value;
        if (!oldValue.equals(value)) {
            table[pos[0]][pos[1]].value = value;
            System.out.println("Replacing " + oldValue + " with " + value);
        }else {
            System.out.println("Existing " + value);
        }
        return oldValue;
    }

    synchronized private void expand() {
        Entry<K, V>[][] oldTable = table;
        MAXN = MAXN * 2;
        table = (Entry<K, V>[][])new Entry[ver][MAXN];

        for (int i = 0; i < ver; i++) {
            counts[i] = 0;
            for (int j = 0; j < MAXN / 2; j++) {
                if (oldTable[i][j] != null) {
                    insertHelper(oldTable[i][j].key, oldTable[i][j].value);
                }
            }
        }

    }


    public V insertHelper(K key, V value) {
        final int COUNT_LIMIT = MAXN;
        K theKey = key;
        V theValue = value;

        for (int count = 0; count < COUNT_LIMIT; count++) {
            //Find a position for insert
            for (int i = 0; i < ver; i++) {
                int hashVal = cuckooHash(i, theKey);
                int index = hashVal % MAXN;
                if (table[i][index] == null) {
                    Entry<K, V> newEntry = new Entry(hashVal, theKey, theValue);
                    table[i][index] = newEntry;
                    counts[i] += 1;
                    System.out.println("Inserted " + theKey + ", " + theValue + " in " + i + ", " + index);
                    return null;
                }
            }
            //No available position
            int ran = r.nextInt(ver);
            int hashVal = cuckooHash(ran, theKey);
            int index = hashVal % MAXN;
            Entry<K, V> oldEntry = table[ran][index];
            table[ran][index] = new Entry(hashVal, theKey, theValue);
            theKey = oldEntry.key;
            theValue = oldEntry.value;
        }
        System.out.println(key + " unable to find a position. Expension needed.");

        //Expension
        expand();
        System.out.println("Expended. Current MAXN = " + MAXN + "\n");
        insertHelper(theKey, theValue);
        return null;
    }


    @Override
    public V put(final K key, final V value){
        System.out.println("\nPutting " + key);
        synchronized (table) {
            int[] pos = findPos(key);
            if (findPos(key) != null) {
                return replace(pos, key, value);
            }
            insertHelper(key, value);
            return null;
        }
    }

    @Override
    public V get(final K key){
        int[] pos = findPos(key);
        if (pos != null) {
            return table[pos[0]][pos[1]].value;
        }
        return null;
    }

    @Override
    public V remove(K key) {
        synchronized (table) {
            int[] pos = findPos(key);
            if (pos != null) {
                V oldValue = table[pos[0]][pos[1]].value;
                table[pos[0]][pos[1]] = null;
                counts[pos[0]] -= 1;
                return oldValue;
            }
            return null;
        }
    }

    @Override
    public boolean remove(K key, V value){
        synchronized (table) {
            int[] pos = findPos(key);
            if (pos != null) {
                if (table[pos[0]][pos[1]].value.equals(value)) {
                    table[pos[0]][pos[1]] = null;
                    counts[pos[0]] -= 1;
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public int size(){
        int count = 0;
        for (int i = 0; i < ver; i++) {
            count += counts[i];
        }
        return count;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(K key){
        return findPos(key) != null;
    }

    @Override
    public V getOrDefault(K key, V defaultValue){
        V value = get(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public Set<K> keySet(){
        Set<K> set = new HashSet<>();
        for (int i = 0; i < ver; i++) {
            for (int j = 0; j < MAXN; j++) {
                if (table[i][j] != null) {
                    set.add(table[i][j].key);
                }
            }
        }
        return set;
    }

    @Override
    public Collection<V> values(){
        Collection<V> collection = new ArrayList<V>();
        for (int i = 0; i < ver; i++) {
            for (int j = 0; j < MAXN; j++) {
                if (table[i][j] != null) {
                    collection.add(table[i][j].value);
                }
            }
        }
        return collection;
    }
}


