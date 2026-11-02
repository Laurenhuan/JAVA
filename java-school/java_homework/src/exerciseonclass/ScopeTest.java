package exerciseonclass;

class Base {
    int x = 1;
    static int y = 2;

    public void methodA() {
        x += 5;
        y += 10;
    }
}

class Derived extends Base {
    int x = 10;
    static int y = 20;

    public void methodB() {
        int x = 100;
        for (int i = 0; i < 3; i++) {
            x += i;
            methodA();
        }

        System.out.println("局部x: " + x);
        System.out.println("实例x: " + this.x);
        System.out.println("父类x: " + super.x);
        System.out.println("静态y: " + y);
        System.out.println("父类静态y: " + super.y);
    }
}

public class ScopeTest {
    public static void main(String[] args) {
        Derived d = new Derived();
        d.methodB();
        System.out.println("最终静态y: " + Derived.y);
    }
}