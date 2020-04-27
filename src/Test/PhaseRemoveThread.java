package Test;

import ConcurrentHashTable.ConcurrentHashTable;

public class PhaseRemoveThread<K, V> implements Runnable {

    private K[] keySet;
    private V[] valueSet;
    private ConcurrentHashTable<K, V> map;

    PhaseRemoveThread(K[] keySet, V[] valueSet, ConcurrentHashTable<K, V> map) {
        this.keySet = keySet;
        this.valueSet = valueSet;
        this.map = map;
    }

    @Override
    public void run() {
        for (int i = 0; i < keySet.length; i++) {
            map.put(keySet[i], valueSet[i]);
        }

        for (int i = 0; i < keySet.length; i++) {
            map.remove(keySet[i]);
        }
    }
}
