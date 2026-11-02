package chapter01;

public class OddNumber {

	public static void main(String[] args) {
		boolean flag = oddNum(1000003823);
		if(flag) {
			System.out.println("奇数");
		}
		else {
			System.out.println("偶数");
		} 
	}
	
	public static boolean oddNum(int n) {
		boolean flag = false;
		if(n%2!=0) {
			flag=true;
		}
		return flag;
	}
}
