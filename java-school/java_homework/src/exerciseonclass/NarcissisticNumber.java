package exerciseonclass;

public class NarcissisticNumber {
	public static boolean isNarcissistic(int n) {
		int original=n;
		int sum=0;
		int shu=n;
		int num=0;
		while(shu!=0) {
			num++;
			shu/=10;
		}
		while(n>0) {
			int digital=n%10;
			int power=1;
			for(int i=0;i<num;i++) {
				power*=digital;
			}
			sum+=power;
			n/=10;
		}
		return sum==original;
	}
	public static void main(String[] args) {
		int num=153;
		if(isNarcissistic(num)) {
			System.out.println(num+"是水仙花数");
		}
		else {
			System.out.println(num+"不是水仙花数");
		}
	}
}
