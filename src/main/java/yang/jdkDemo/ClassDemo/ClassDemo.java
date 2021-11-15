package yang.jdkDemo.ClassDemo;

public class ClassDemo {
    String a = "1";
    String b = "2";
    String c = "12";
    public static void main(String[] args) {
        ClassDemo classDemo = new ClassDemo();
        String d = classDemo.a + "2";
        System.out.println(d == classDemo.c);
    }
}