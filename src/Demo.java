import ConcurrentHashTable.ConcurrentHashTable;
import LockBased.*;
import LockFree.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Demo {
    public static void main(String[] args) {

        //ConcurrentHashTable<String, String> c = new LockFreeHashMap<>();
        ConcurrentHashTable<Integer, Integer> c = new CuckooLockBasedHashTable<>(100);
        boolean b_empty = c.isEmpty();

        int num1 = 1;
        int num2 = 12;
        int num3 = 133;
        int num4 = 1464;
        //c.put(num1, num1);
        //c.put(num2, num2);
        //c.put(num3, num3);
        //c.put(num4, num4);
        //c.put("shuqi","321");
        //c.put("xiyu","111");
        //c.put("yuesen","000");

        int a = c.size();
        boolean b = c.isEmpty();
        boolean d = c.containsKey(num1);
        boolean e = c.containsKey(num2);
        //c.remove("shuqi");
        //c.remove("jun","123");
        //c.remove("jun","321");
        //c.remove("haha");
        /**
        System.out.println(c.findPos(0));
        System.out.println(c.findPos(num1)[0]);
        System.out.println(c.findPos(num1)[1]);
        System.out.println(c.findPos(num2)[0]);
        System.out.println(c.findPos(num2)[1]);
        System.out.println(c.findPos(num3)[0]);
        System.out.println(c.findPos(num3)[1]);
        System.out.println(c.findPos(num4)[0]);
        System.out.println(c.findPos(num4)[1]);
         */
        System.out.println(c.values());
        System.out.println(b_empty);
        System.out.println(a);
        System.out.println(b);
        System.out.println(d);
        System.out.println(e);


        for (int i = 0; i < 100; i++) {
            c.put(i, i);
            System.out.println("Now total number of " + c.values().size());
            System.out.println("Now size" + c.size());
        }

        for (int i = 0; i < 110; i++) {
            c.put(i, i);
            System.out.println("Now total number of " + c.values().size());
            System.out.println("Now size" + c.size());
        }


        for (int i = 99; i < 110; i++) {
            c.put(i, i);
            System.out.println(c.values().size());
            System.out.println("Now size" + c.size());
        }
        System.out.println(c.values().size());
        System.out.println("Now size" + c.size());

        List<Integer> realValues = new ArrayList<>(c.values());
        Collections.sort(realValues);
        for (int i = 0; i < 109; i++) {
            if (realValues.get(i) == realValues.get(i + 1)) {
                System.out.println("Repeat at " + i);
            }
        }
    }


}
