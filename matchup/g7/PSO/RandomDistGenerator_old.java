package matchup.g7.PSO;

import javafx.util.Pair;

public class RandomDistGenerator_old {
	// Inspired by Mike Housky on StackOverflow:
	// https://stackoverflow.com/questions/18448417/create-constrained-random-numbers/18448874#18448874
	
	public RandomDistGenerator_old(Pair<Integer, Integer>[] range, int target) {
		// TODO
	}
	
	/**
	 * return a convolutional vector of size width
	 * @param vector the original vector
	 * @param width the width of convolution
	 * @return the convolutional vector
	 */
	private static int[] stepConvolution(int[] vector, int width) {
		int result[] = new int[vector.length + width];
		for (int i = 0; i < vector.length; i++) {
			result[i + width] = vector[i];
		}
		
		int sum = 0;
		for (int i = 0; i <= width; i++)
			sum += result[i];
		
		result[0] = sum;
		for (int i = 1; i < result.length; i++) {
			sum -= result[i - 1];
			if (i < vector.length)
				sum += result[i + width];
			result[i] = sum;
		}
		return result;
	}
	
	
}
