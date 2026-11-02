package exercise01;

class Circle {
    private double radius;  

    public Circle() {
        this.radius = 0;
    }

    public Circle(double r) {
        this.radius = r;
    }

    public double getArea() {
        return Math.PI * radius * radius;
    }

    public double getPerimeter() {
        return 2 * Math.PI * radius;
    }

    public void show() {
        System.out.println("圆的半径：" + radius);
        System.out.println("圆的周长：" + getPerimeter());
        System.out.println("圆的面积：" + getArea());
    }
}

class Cylinder extends Circle {
    private double hight;  
    public Cylinder(double r, double h) {
        super(r);   
        this.hight = h;
    }

    public double getVolume() {
        return getArea() * hight; 
    }

    public void showVolume() {
        System.out.println("圆柱体的体积：" + getVolume());
    }
}

public class Test {
    public static void main(String[] args) {
        Circle c = new Circle(5.0);
        c.show();   

        System.out.println(); 
        Cylinder cyl = new Cylinder(4.0, 10.0);
        cyl.show();           
        cyl.showVolume();      
    }
}

