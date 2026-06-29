import org.testng.Assert;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Demo1 {

    public static void main(String[] args) {

    var list1 = new ArrayList<>(Arrays.asList(null, "a", "b", "c")); // mutable
        var list2 = new ArrayList<>(Arrays.asList("a", "c", "b"));

        list1.retainAll(list2); // ✅ hoạt động bình thường
        System.out.println(list1); // [a, b, c]
        System.out.println(list1.containsAll(list2));
        System.out.println(list1);
        System.out.println(list1.retainAll(list2));
        System.out.println(list2);
        Assert.assertTrue(list1.containsAll(list2));
    }
    public void hel(){}
}
