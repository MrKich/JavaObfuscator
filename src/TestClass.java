/**
 * Created by azarn on 5/18/16.
 */
public class TestClass {
    private String test;

    TestClass(String s) {
        test = s;
    }

    public void go() {
        System.out.println("My String test is " + test);
    }

    public static void main(String args[]) {
        if (args.length == 1) {
            new TestClass(args[0]).go();
        } else {
            new TestClass("NO_DEFAULT_ARG").go();
        }
    }
}
