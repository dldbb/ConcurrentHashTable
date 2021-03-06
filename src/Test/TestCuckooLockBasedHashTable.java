package Test;

import ConcurrentHashTable.ConcurrentHashTable;
import LockBased.CuckooLockBasedHashTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class TestCuckooLockBasedHashTable {

    private static final int TEST_SIZE = 10000;
    private static final int NUM_BUCKET = 10000;

    private ConcurrentHashTable<Integer, Integer> concurrentHashTable;
    private Integer[] testSet1 = new Integer[TEST_SIZE];
    private Integer[] testSet2 = new Integer[2 * TEST_SIZE];
    private Integer[] testSet3 = new Integer[3 * TEST_SIZE];
    private Integer[] testSet4 = new Integer[TEST_SIZE];

    @Before
    public void init() {
        for (int i = 0; i < 3 * TEST_SIZE; i++) {
            testSet3[i] = i;
            if (i < 2 * TEST_SIZE) {
                testSet2[i] = i;
            }
            if (i < TEST_SIZE) {
                testSet1[i] = i;
            }
        }

        for (int i = 0; i < TEST_SIZE; i++) {
            testSet4[i] = 3 * TEST_SIZE + i;
        }
    }

    @Test
    public void testCuckooLockBasedHashTableCanPut() {
        concurrentHashTable = new CuckooLockBasedHashTable<>(NUM_BUCKET);
        makePutThread(concurrentHashTable);
        Assert.assertEquals(3 * TEST_SIZE, concurrentHashTable.size());

        List<Integer> expectKeys = Arrays.asList(testSet3);
        List<Integer> expectValues = Arrays.asList(testSet3);

        Set<Integer> res = concurrentHashTable.keySet();
        List<Integer> realKeys = new ArrayList<>(res);
        // have to sort  because the order is not the same
        Collections.sort(realKeys);
        Assert.assertEquals(expectKeys, realKeys);

        List<Integer> realValues = new ArrayList<>(concurrentHashTable.values());
        // have to sort  because the order is not the same
        Collections.sort(realValues);
        Assert.assertEquals(expectValues, realValues);
    }

    @Test
    public void testCuckooLockBasedHashTableCanRemove() {
        concurrentHashTable = new CuckooLockBasedHashTable<>(NUM_BUCKET);
        makeRemoveThread(concurrentHashTable);
        Assert.assertEquals(TEST_SIZE, concurrentHashTable.size());

        List<Integer> expectKeys = Arrays.asList(testSet4);
        List<Integer> expectValues = Arrays.asList(testSet4);

        Set<Integer> res = concurrentHashTable.keySet();
        List<Integer> realKeys = new ArrayList<>(res);
        // have to sort  because the order is not the same
        Collections.sort(realKeys);
        Assert.assertEquals(expectKeys, realKeys);

        List<Integer> realValues = new ArrayList<>(concurrentHashTable.values());
        // have to sort  because the order is not the same
        Collections.sort(realValues);
        Assert.assertEquals(expectValues, realValues);

    }

    private void makePutThread(ConcurrentHashTable<Integer, Integer> table) {

        Thread[] threads = new Thread[3];
        threads[0] = new Thread(new PutThread<Integer, Integer>(testSet1, testSet1, table));
        threads[1] = new Thread(new PutThread<Integer, Integer>(testSet2, testSet2, table));
        threads[2] = new Thread(new PutThread<Integer, Integer>(testSet3, testSet3, table));
        threads[1].start();
        threads[0].start();
        threads[2].start();

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void makeRemoveThread(ConcurrentHashTable<Integer, Integer> table) {

        Thread[] threads = new Thread[4];
        threads[0] = new Thread(new RemoveThread<Integer, Integer>(testSet1, testSet1, table));
        threads[1] = new Thread(new RemoveThread<Integer, Integer>(testSet3, testSet3, table));
        threads[2] = new Thread(new RemoveThread<Integer, Integer>(testSet2, testSet2, table));
        threads[3] = new Thread(new PutThread<Integer, Integer>(testSet4, testSet4, table));
        threads[3].start();
        threads[0].start();
        threads[2].start();
        threads[1].start();

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
