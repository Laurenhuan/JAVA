package exerciseonclass;

class Shape {
    public String draw(Diamond d) {
        return "Shape draws Diamond";
    }
    public String draw(Shape s) {
        return "Shape draws Shape";
    }
}

class Rectangle extends Shape {
    public String draw(Rectangle r) {
        return "Rectangle draws Rectangle";
    }
    public String draw(Shape s) {
        return "Rectangle draws Shape";
    }
}

class Triangle extends Rectangle {}
class Diamond extends Rectangle {}

public class DrawingTest {
    public static void main(String[] args) {
        Shape s1 = new Shape();
        Shape s2 = new Rectangle();
        Rectangle r = new Rectangle();
        Triangle t = new Triangle();
        Diamond d = new Diamond();

        System.out.println("1--" + s1.draw(t));
        System.out.println("2--" + s1.draw(d));
        System.out.println("3--" + s2.draw(r));
        System.out.println("4--" + s2.draw(t));
        System.out.println("5--" + s2.draw(d));
        System.out.println("6--" + r.draw(r));
        System.out.println("7--" + r.draw(t));
        System.out.println("8--" + r.draw(d));
    }
}