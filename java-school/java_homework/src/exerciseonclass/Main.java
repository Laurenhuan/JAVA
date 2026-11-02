
package exerciseonclass;

class Parent {
	public int f(int a,int b) {
		while(b!=0) {
			int temp=a%b;
			a=b;
			b=temp;
		}
		return a;
	}
}
class Child extends Parent{
	public int f(int a,int b) {
		int m=super.f(a, b);
		return a*b/m;
	}
}

public class Main{
	public static void main(String[] args) {
		Parent p=new Parent();
		int yueshu=p.f(12,18);
		System.out.println("最大公约数："+yueshu);
		Child c=new Child();
		int yinshu=c.f(12, 18);
		System.out.println("最大公因数："+yinshu);
	}
}
