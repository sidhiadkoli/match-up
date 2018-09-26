package matchup.g7.PSO;

import java.util.Random;

public class Swarm {
	// The number of particles
	private final int n;
	
	// The dimensions for each particle
	private final int d;
	
	private Particle particles[];
	
	// The global and local attraction weight
	private double wg, wl;
	
	// Time interval
	double time;
	
	// Evaluation function for particles
	FitnessEvaluation ev;
	
	// The current global best value of all particles
	public double globalBest = -Double.MAX_VALUE;
	
	// The coordinate when the global best was reached
	double globalx[];
	
	// Random generator
	Random rd = new Random();
	
	public Swarm(int n, int d, double wg, double wl, double time, FitnessEvaluation ev) {
		this.n = n;
		this.d = d;
		this.wg = wg;
		this.wl = wl;
		this.time = time;
		this.ev = ev;
		initParticles();
	}
	
	private double[] initDistribution() {
		//return RandomDistGenerator.randomDist().stream().mapToInt(Integer::intValue).toArray();
		Random rand = new Random();
		double sum = 0.0D;
		double[] dist = new double[d];
		for (int i = 0; i < d; i++) {
			dist[i] = rand.nextDouble();
			sum += dist[i];
		}
		for (int i = 0; i < d; i++) {
			dist[i] *= 75.0D / sum;
			dist[i] += 1.0D;
			if (dist[i] < 1.0D || dist[i] > 11.0D)
				return initDistribution();
		}
		
		
		/*sum = 0;
		for (int i = 0; i < d; i++) {
			sum += dist[i];
		}
		if (Math.abs(sum - 90) > 0.1D)
			System.out.println("This is wrong!!!!");
		*/
		return dist;
	}
	
	public void updateEv(FitnessEvaluation ev) {
		this.ev = ev;
		globalBest = -Double.MAX_VALUE;
		for (Particle p : particles) {
			p.updateEv(ev);
			updateGlobalBest(p);
		}
	}
	
	private void initParticles() {
		particles = new Particle[n];
		for (int i = 0; i < n; i++) {
			particles[i] = new Particle(initDistribution(), ev);
			particles[i].updateVelocity(randomVectorWithinRange());
			updateGlobalBest(particles[i]);
		}
	}
	
	private boolean updateGlobalBest(Particle p){
		boolean flag = p.getFitness() > globalBest;
		if (flag){
			globalx = p.getLocalX();
			/*double sum = 0.0D;
			for (double x : globalx)
				sum += x;
			System.out.println(sum);*/
			globalBest = p.getFitness();
		}
		return flag;
	}
	
	// Time efficiency: t * n * d
	public void update(int epoches) {
		if (globalBest < -100000.0D)
			initParticles();
		for (int i = 0; i < epoches; i++) {
			for (Particle p : particles) {
				// Update the particle's position
				p.moveParticle(time);
				// Evaluate the particle's fitness
				p.evaluate();
				// Update the particle's velocity vector
				double deltaV[] = new double[d];
				double randG = rd.nextDouble(), randL = rd.nextDouble();
				double random[] = randomVectorWithinRange();
				for (int j = 0; j < d; j++) {
					deltaV[j] = wg * randG * (globalx[j] - p.x[j]) + 
							wl * randL * (p.getLocalX()[j] - p.x[j]) + random[j];
				}
				p.updateVelocity(deltaV);
				// Update local best and global best
				if (p.updateLocalBest()) updateGlobalBest(p);
			}
		}
	}

	public int[] normalizeGlobal() {
		int x_int[] = new int[d];
		double x_frac[] = new double[d];
		int sum = 0;
		for (int i = 0; i < d; i++) {
			x_int[i] = (int)Math.round(globalx[i]);
			sum += x_int[i];
			x_frac[i] = globalx[i] - x_int[i];
			// System.out.println(globalx[i]);
		}
		if (sum < 90) 
			for (int i = 90 - sum; i > 0; i--) {
				int maxIndex = maxIndex(x_frac);
				x_int[maxIndex] += 1;
				x_frac[maxIndex] -= 1.0D;
			}
		else 
			for (int i = sum - 90; i > 0; i--) {
				int minIndex = minIndex(x_frac);
				x_frac[minIndex] += 1.0D;
				if (x_int[minIndex] == 1) {
					i++;
				}
				else
					x_int[minIndex] -= 1;
			}
		
		for (int i = 0; i < d; i++) {
			if (x_int[i] == 0)
				System.out.println(globalx[i]);
		}
		return x_int;
	}
	
	/**
	 * Generate a random vector in dimension d with its L1 distance as 0.
	 * @return a length-d array of random numbers
	 */
	private double[] randomVectorWithinRange(){
		double v[] = new double[d];
		double sum = 0.0D;
		for (int i = 0; i < d; i++) {
			v[i] = rd.nextDouble();
			sum += v[i];
		}
		
		for (int i = 0; i < d; i++) {
			v[i] /= sum;
			v[i] -= 1.0D / d;
			v[i] *= 10.0D;
		}
		
		return v;
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
			if (array[i] > max) {
				max = array[i];
				maxIndex = i;
			}
		return maxIndex;
	}
	
	public static void main(String[] args) {
		Swarm swarm = new Swarm(20, 15, 0.7, 0.3, 0.5, x -> (x[11] - x[0]));
		double sum = 0;
		double max = -10;
		for (double entry : swarm.randomVectorWithinRange()) {
			sum += entry;
			if (entry > max)
				max = entry;
		}
		//for (int entry : swarm.normalizeGlobal())
		System.out.println(max);
		System.out.println(sum);
	}
	
}
