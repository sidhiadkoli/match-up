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
	double globalBest = -Double.MAX_VALUE;
	
	// The coordinate when the global best was reached
	double globalx[] = new double[11];
	
	// Random generator
	Random rd = new Random();
	
	public Swarm(int n, int d, double wg, double wl, double time) {
		this.n = n;
		this.d = d;
		this.wg = wg;
		this.wl = wl;
		this.time = time;
		initParticles();
	}
	
	private void initParticles() {
		particles = new Particle[n];
		for (int i = 0; i < n; i++) {
			particles[i] = new Particle(new double[d], ev); //TODO
			particles[i].updateVelocity(randomVectorWithinRange());
			updateGlobalBest(particles[i]);
		}
	}
	
	private boolean updateGlobalBest(Particle p){
		boolean flag = p.getFitness() > globalBest;
		if (flag){
			globalx = p.getLocalX();
			globalBest = p.getFitness();
		}
		return flag;
	}
	
	// Time efficiency: t * n * d
	public void update(int epoches) {
		for (int i = 0; i < epoches; i++) {
			for (Particle p : particles) {
				// Update the particle's position
				p.moveParticle(time);
				// Evaluate the particle's fitness
				p.evaluate();
				// Update the particle's velocity vector
				double deltaV[] = new double[d];
				double randG = rd.nextDouble(), randL = rd.nextDouble();
				for (int j = 0; j < d; j++) {
					deltaV[j] = wg * randG * (globalx[j] - p.x[j]) + 
							wl * randL * (p.getLocalX()[j] - p.x[j]);
				}
				p.updateVelocity(deltaV);
				// Update local best and global best
				if (p.updateLocalBest()) updateGlobalBest(p);
			}
		}
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
			v[i] -= 1.0D;
		}
		
		return v;
	}
}
