package exerciseonclass;

public class ArrayOperation {
	public static void main(String[] args) {
		int[][] matrix = new int[][]{{1,2,3},{4,5,6},{7,8,9}};
		rotateRow(matrix,0);
		swapColumns(matrix,0,2);
		reverseDiagonal(matrix);
		printMatrix(matrix);
	}
	public static void rotateRow(int[][] arr, int row) {
	    if (row < 0 || row >= arr.length || arr[row].length == 0) return;
	    int last = arr[row][arr[row].length - 1];
	    for (int i = arr[row].length - 1; i > 0; i--) {
	        arr[row][i] = arr[row][i - 1];
	    }
	    arr[row][0] = last;
	}

	public static void swapColumns(int[][] arr, int col1, int col2) {
	    for (int i = 0; i < arr.length; i++) {
	        int temp = arr[i][col1];
	        arr[i][col1] = arr[i][col2];
	        arr[i][col2] = temp;
	    }
	}

	public static void reverseDiagonal(int[][] arr) {
	    int n = arr.length; // 假设是方阵
	    for (int i = 0; i < n / 2; i++) {
	        int temp = arr[i][i];
	        arr[i][i] = arr[n - 1 - i][n - 1 - i];
	        arr[n - 1 - i][n - 1 - i] = temp;
	    }
	}

	public static void printMatrix(int[][] matrix) {
	    for (int i = 0; i < matrix.length; i++) {
	        for (int j = 0; j < matrix[i].length; j++) {
	            System.out.print(matrix[i][j] + " ");
	        }
	        System.out.println();
	    }
	}
}
 