package PhaseConcurrent;

import ConcurrentHashTable.ConcurrentHashTable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Jun Wang
 * implementation of lock free concurrent hash map.
 */
public class PhaseConcurrentHashTable<K, V> implements ConcurrentHashTable<K, V> {

    private int numSlots;
    private List<AtomicReference<MapEntry<K, V>>> table;

    public PhaseConcurrentHashTable(int numSlots) {
        this.numSlots = numSlots;
        this.table = new ArrayList<>();

        for (int i = 0; i < this.numSlots; i++) {
            table.add(new AtomicReference<>(null));
        }
    }

    @Override
    public int hash(K key) {
        return Math.abs(key.hashCode() % this.numSlots);
    }

    @Override
    public V put(K key, V value) {
        int index = hash(key);
        MapEntry<K, V> entry = new MapEntry<>(key, value);

        while (index < numSlots) {

            MapEntry<K, V> iter = table.get(index).get();

            if (iter == null) {
                table.get(index).compareAndSet(null, entry);
                return entry.value;
            } else if (iter.key.equals(entry.key)) {
                if(table.get(index).compareAndSet(iter,entry)){
                    return iter.value;
                }
            } else if (iter.priority < entry.priority) {
                index++;
            } else if (table.get(index).compareAndSet(iter, entry)) {
                entry = iter;
                index++;
            }
        }
        return null;
    }

    @Override
    public V get(K key) {
        int index = hash(key);

        while(index < numSlots){
            MapEntry<K,V> entry = table.get(index).get();
            if(entry == null || entry.priority > key.hashCode()){
                return null;
            }

            if(entry.key.equals(key)){
                return entry.value;
            }
            index++;
        }

        return null;
    }

    @Override
    public V remove(K key) {
        int index = hash(key);

        while(index < numSlots){
            MapEntry<K,V> entry = table.get(index).get();
            if(entry == null || entry.priority > key.hashCode()){
                return null;
            }

            if(entry.key.equals(key)){
                if(table.get(index).compareAndSet(entry, null)){
                    return entry.value;
                }
            }else {
                index++;
            }
        }

        return null;

    }

    @Override
    public boolean remove(K key, V value) {
        int index = hash(key);

        while(index < numSlots){
            MapEntry<K,V> entry = table.get(index).get();
            if(entry == null || entry.priority > key.hashCode()){
                return false;
            }

            if(entry.key.equals(key) && entry.value.equals(value)){
                if(table.get(index).compareAndSet(entry, null)){
                    return true;
                }
            }else {
                index++;
            }
        }

        return false;
    }

    @Override
    public int size() {
        int count = 0;
        for(int i = 0; i < table.size(); i++){
            if(table.get(i).get() != null){
                count++;
            }
        }

        return count;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        V value = get(key);
        return value == null? defaultValue: value;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keySet = new HashSet<>();

        for(int i = 0; i < table.size(); i++){
            if(table.get(i).get() != null){
                keySet.add(table.get(i).get().key);
            }
        }
        return keySet;
    }

    @Override
    public Collection<V> values() {
        Collection<V> keySet = new ArrayList<>();

        for(int i = 0; i < table.size(); i++){
            if(table.get(i).get() != null){
                keySet.add(table.get(i).get().value);
            }
        }
        return keySet;
    }
}
