package exerciseonclass;
import java.util.Scanner;
public class PalindromeChecker {
	public static void main(String[] args) {
		Scanner scanner=new Scanner(System.in);
		System.out.print("请输入一个整数");
		int number=scanner.nextInt();
		if(isPalindrome(number)) {
			System.out.println(number+"是回文数");
		}
		else {
			System.out.println(number+"不是回文数");
		}
	}
	
	public static boolean isPalindrome(int num) {
		if(num<0) {
			return false;
		}
		int original=num;
		int reversed=0;
		while(num!=0) {
			int digital=num%10;
			reversed=reversed*10+digital;
			num/=10;
		}
		if(reversed==original) {
			return true;
		}
		else {
			return false;
		}
	}
}
