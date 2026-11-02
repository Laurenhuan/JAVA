package exerciseonclass;
import java.util.Scanner;
public class Input {
	public static void main(String[] args) {
		Scanner scanner=new Scanner(System.in);
		int n=scanner.nextInt();
		long sum=0;
		if(n>=1&&n<=15) {
			for(int i=1;i<=n;i++) {
				sum+=jiecheng(i);
			}
			System.out.println("计算出来的答案为"+sum);
		}
		else {
			System.out.println("你输入的不符合要求(n<=15和n>=1)，请重新输入~");
		}
	}
	public static long jiecheng(int n) {
		long digit=1;
		for(int i=1;i<=n;i++) {
			digit*=i;
		}
		return digit;
	}
}
