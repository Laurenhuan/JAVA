package chapter01;

public class ShiftOperators {

	public static void main(String[] args) {
		System.out.println(Integer.toBinaryString(8));
		System.out.println(Integer.toBinaryString(-8));
		System.out.println(Integer.toBinaryString((byte)(-8>>2)));
		System.out.println(Integer.toBinaryString((byte)(-8>>>2)));
		System.out.println(Integer.toBinaryString((byte)(8>>2)));
		System.out.println(Integer.toBinaryString((byte)(8>>>2)));
		System.out.println(-8>>>2);
		System.out.println(-8>>2);
		System.out.println(8>>>2);
		System.out.println(8>>2);
		System.out.println(((byte)(-8>>>2)));
		System.out.println(((byte)(-8>>2)));
		System.out.println(((byte)(8>>>2)));
		System.out.println(((byte)(8>>2)));

		
		
		byte j = 40;
		byte j1 = (byte) (j<<1);
		byte j2 = (byte) (j<<2);
		System.out.println("j=" + j);
		System.out.println("j1=" + j1);
		System.out.println("j2=" + j2);
	}
}
