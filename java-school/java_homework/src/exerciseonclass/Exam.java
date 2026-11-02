package exerciseonclass;

public class Exam {
	public static void main(String[] args) {
		int[] dpm= {0,31,28,31,30,31,30,31,31,30,31,30,31};
		int year=2010;
		int month=5;
		int day=1;
		if(isLeapYear(year)) {
			dpm[2]=29;
		}
		int totalDays=0;
		for(int i=1;i<month;i++) {
			totalDays+=dpm[i];
		}
		
		if(((totalDays+day-1)%5)<3) {
			System.out.println("1");
		}
		else {
			System.out.println("0");
		}
	}
	public static boolean isLeapYear(int year) {
		return(year%4==0&&year%400!=0)||(year%400==0);
	}
}
