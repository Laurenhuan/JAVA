package chapter01;

public class LogicalOperators {

	public static void main(String[] args) {
		boolean result;
		result = 0 <= 100 && 200 < 100;
		System.out.println(result);
		
		result = 20 <= 25 && !(40 > 15) || 20 >= 25;
		System.out.println(result);
	}
}
