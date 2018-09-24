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
	double localx[];
	
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
	
	void updateEv(FitnessEvaluation ev) {
		this.ev = ev;
		localy = -Double.MAX_VALUE;
		evaluate();
		updateLocalBest();
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
		/*
		double sum = 0.0D;
		for (double i : x) {
			sum += i;
		}
		System.out.println(sum);
		*/
		y = ev.evaluate(x);
	}
	
	boolean updateLocalBest() {
		boolean flag = (y > localy);
		if (flag) {
			localx = x.clone();
			localy = y;
			//System.out.println(y);
		}
		return flag;
	}
	


	
	
}
