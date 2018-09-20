package matchup.g7.PSO;

import java.util.Arrays;

class Particle {
	private final int dimension; 
	
	double x[];
	private double vx[]; 
	
	private FitnessEvaluation ev;
	private double y;
	
	// local best
	private double localy = -Double.MAX_VALUE;
	private double localx[] = new double[11];
	
	Particle(double[] x, FitnessEvaluation ev) {
		this.x = x;
		dimension = x.length;
		vx = new double[dimension]; // 0 by default
		this.ev = ev;
		evaluate();
		updateLocalBest();
	}
	
	Particle(int[] x, FitnessEvaluation ev) {
		this(Arrays.stream(x).asDoubleStream().toArray(), ev);
	}
	
	double getFitness() {
		return y;
	}
	
	double[] getLocalX() {
		return localx;
	}
	
	void moveParticle(double time) {
		for (int i = 0; i < dimension; i++)
			x[i] += vx[i] * time;
	}
	
	void updateVelocity(double[] vx) {
		for (int i = 0; i < dimension; i++)
			this.vx[i] += vx[i];
	}
	
	void evaluate() {
		y = ev.evaluate(this);
	}
	
	boolean updateLocalBest() {
		boolean flag = (y > localy);
		if (flag) {
			localx = x.clone();
			localy = y;
		}
		return flag;
	}
	
	private int minIndex(double[] array){
		double min = Double.MAX_VALUE;
		int minIndex = -1;
		for (int i = 0; i < array.length; i++)
			if (array[i] < min) {
				min = array[i];
				minIndex = i;
			}
		return minIndex;
	}
	
	private int maxIndex(double[] array){
		double max = -Double.MAX_VALUE;
		int maxIndex = -1;
		for (int i = 0; i < array.length; i++)
			if (array[i] < max) {
				max = array[i];
				maxIndex = i;
			}
		return maxIndex;
	}
	
	int[] normalize() {
		int x_int[] = new int[dimension];
		double x_frac[] = new double[dimension];
		int sum = 0;
		for (int i = 0; i < dimension; i++) {
			x_int[i] = (int)Math.round(x[i]);
			sum += x_int[i];
			x_frac[i] = x[i] - x_int[i];
		}
		if (sum < 90) 
			for (int i = 90 - sum; i > 0; i--) {
				x_int[maxIndex(x_frac)] += 1;
				x_frac[maxIndex(x_frac)] -= 1.0D;
			}
		else 
			for (int i = sum - 90; i > 0; i--) {
				x_int[minIndex(x_frac)] -= 1;
				x_frac[minIndex(x_frac)] += 1.0D;
			}
		return x_int;
	}
	
	
}
