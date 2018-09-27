package matchup.g7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

import matchup.g7.PSO.FitnessEvaluation;
import matchup.g7.PSO.Swarm;


public class Player implements matchup.sim.Player {
	
	// The list of all player skill levels
	private List<Integer> skills;
	private List<List<Integer>> distribution;
	private List<Float> averageStrength;
	private List<Integer> availableRows;
	private List<Integer> opponentRemainSkills;
	// keep track of history distribution
	private double dic[][] = new double[15][11];
	private List<List<Integer>> opponentDistribution;
	private int rounds = 0;
    // add counter variance strategies
    private List<Float> home_line;
    private float opponentVar;

	private FitnessEvaluation ev;
	private Swarm swarm;
	
	private boolean state; // Whether the player is playing as the home team
	
	public Player() {
		skills = new ArrayList<Integer>(Arrays.asList(1, 2, 2, 2, 3, 7, 7, 7, 7, 7, 9, 9, 9, 9, 9));
		availableRows = new ArrayList<Integer>(Arrays.asList(0, 1, 2));
		averageStrength = new ArrayList<Float>();
		opponentDistribution = new ArrayList<List<Integer>>();
		state = false;

		for (int i = 0; i < 15; i++)
			for (int j = 0; j < 11; j++)
				dic[i][j] = 1.0D / 11.0D;
		ev = new FitnessEvaluation() {
			@Override
			public double evaluate(double[] x) {
				double score = 0.0D;
				for (int i = 0; i < x.length; i++) {
					for (int j = 0; j < dic[i].length; j++) {
						// Step-based evaluation, +1 for win and -1 for lose
						if ((x[i] > 11.0D) || (x[i] < 1.0D))
							return -Double.MAX_VALUE / 2;
						else if (x[i] - 1 >= j + 3)
							score += dic[i][j];
						else if (x[i] - 1 < j - 2)
							score -= dic[i][j];
						/*
						if ((x[i] > 11.0D) || (x[i] < 1.0D))
							return -Double.MAX_VALUE / 2;
						else if (x[i] >= j + 3)
							score += dic[i][j] * (11 - x[i]);
						else if (x[i] < j - 2)
							score += dic[i][j] * (j - 3 - x[i]);
						else 
							score += dic[i][j] * (j + 2 - x[i]);
						*/
					}
				}
				return score;
			}
		};
		
		swarm = new Swarm(400, 15, 0.7D, 0.3D, 0.25D, ev);
	}
	
	@Override
	public void init(String opponent) {
        home_line = new ArrayList<Float>();
        opponentVar = 0;
	}


	@Override
	public List<Integer> getSkills() {
		rounds++;
		if (rounds > 1)
			logHistory();
		return skills;
	}

	@Override
	public List<List<Integer>> getDistribution(List<Integer> opponentSkills, boolean isHome) {
		state = isHome;
		distribution = new ArrayList<List<Integer>>();
		this.opponentRemainSkills = new ArrayList<Integer>(opponentSkills);
		opponentVar = findVariance(opponentSkills);

		if (isHome) {
			distribution.add(new ArrayList<Integer>(Arrays.asList(skills.get(0), skills.get(1), skills.get(2), skills.get(3), skills.get(4))));
			distribution.add(new ArrayList<Integer>(Arrays.asList(skills.get(5), skills.get(6), skills.get(7), skills.get(8), skills.get(9))));
			distribution.add(new ArrayList<Integer>(Arrays.asList(skills.get(10), skills.get(11), skills.get(12), skills.get(13), skills.get(14))));
		}
		else {
            distribution = counter_var(home_line);
        }		

		for (int i=0; i<distribution.size(); i++){
			averageStrength.add(findAverage(distribution.get(i)));
		}

		return new ArrayList<List<Integer>>(distribution);
	}
	
	public int sum(List<Integer> list) {
     	int sum = 0; 

     	for (int i : list)
         	sum = sum + i;

    	return sum;
	}

	/**
	 * Compute the optimal distribution to counter against the opponent based on variance
	 * @param row The index of our line used
	 * @param opponentRound The list of player skills in the opponent line
	 * @return a pair containing score difference and the optimal permutation
	 */
	
    public List<List<Integer>> counter_var(List<Float> line){
        float sum = 0;
        for (Float i : line)
            sum = sum + i;
        List<List<Integer>> result = new ArrayList<List<Integer>>();
        if (sum > 0){
            Collections.sort(skills);
            for (int i=0 ; i< 3; i++){
                List<Integer> temp = new ArrayList<Integer>();
                for (int j=0; j<5; j++){
                    temp.add(skills.get(i*5+j));
                }
                result.add(temp);
            }
        }
        else{
            Collections.sort(skills);
            for (int i=0 ; i< 3; i++){
                List<Integer> temp = new ArrayList<Integer>();
                for (int j=0; j<5; j++){
                    temp.add(skills.get(i+j*3));
                }
                result.add(temp);
            }

        }
        return result;
    }
    

	private void logHistory() {
		if (opponentDistribution.size() != 0) {
			Collections.sort(opponentDistribution, (l1, l2) -> {
				if (findAverage(l1) > findAverage(l2))
					return 1;
				else if (findAverage(l1) < findAverage(l2))
					return -1;
				else
					if (findVariance(l1) > findVariance(l2))
						return 1;
					else if (findVariance(l1) < findVariance(l2))
						return -1;
					else
						return 0;
			});
			for (int i = 0; i < opponentDistribution.size(); i++) {
				List<Integer> line = opponentDistribution.get(i);
				Collections.sort(line);
				for (int j = 0; j < line.size(); j++) {
					dic[5 * i + j][line.get(j) - 1] += (2 / rounds);
					for (int k = 0; k < dic[5 * i + j].length; k++) {
						dic[5 * i + j][k] /= ((rounds + 2) / rounds);
					}
				}
			}
			opponentDistribution.clear();
		}
		swarm.updateEv(new FitnessEvaluation() {
			@Override
			public double evaluate(double[] x) {
				double score = 0.0D;
				for (int i = 0; i < x.length; i++) {
					for (int j = 0; j < dic[i].length; j++) {
						// Step-based evaluation, +1 for win and -1 for lose
						if ((x[i] > 11.0D) || (x[i] < 1.0D))
							return -Double.MAX_VALUE / 2;
						else if (x[i] - 1 >= j + 3)
							score += dic[i][j];
						else if (x[i] - 1 < j - 2)
							score -= dic[i][j];
						/*
						if ((x[i] > 11.0D) || (x[i] < 1.0D))
							return -Double.MAX_VALUE / 2;
						else if (x[i] >= j + 3)
							score += dic[i][j] * (11 - x[i]);
						else if (x[i] < j - 2)
							score += dic[i][j] * (j - 3 - x[i]);
						else 
							score += dic[i][j] * (j + 2 - x[i]);
							*/
						
					}
				}
				return score;
			}
		});
		
		swarm.update(50);
		//System.out.println(swarm.globalBest);
		skills = Arrays.stream(swarm.normalizeGlobal()).boxed().collect(Collectors.toList());
	}
	

	public static void print(String str){
		System.out.println(str);
	}

	private static float findAverage(List<Integer> line) {
		int sum = 0;
		for (int i : line) sum += i;
		return sum / (float)line.size();
	}
	
	private static float findVariance(List<Integer> line) {
		float sum = 0.0F;
		float mean = findAverage(line);
		for (int i : line){
			sum += i * i;
		}
		return sum - mean * mean / line.size();
	}
	
	/**
	 * Find out the optimal permutation of our line against the opponent
	 * @param row The index of our line used
	 * @param opponentRound The list of player skills in the opponent line
	 * @return a pair containing score difference and the optimal permutation
	 */
	private Pair permutation(int row, List<Integer> opponentRound) {
		ArrayList<ArrayList<Integer>> all_possible = new ArrayList<ArrayList<Integer>>();
 		all_possible.add(new ArrayList<Integer>());
 		List<Integer> line = distribution.get(row);
		for (int i = 0; i < line.size(); i++) {
			//list of list in current iteration of the array num
			ArrayList<ArrayList<Integer>> current = new ArrayList<ArrayList<Integer>>();
 
			for (ArrayList<Integer> l : all_possible) {
				// # of locations to insert is largest index + 1
				for (int j = 0; j < l.size()+1; j++) {
					// + add num[i] to different locations
					l.add(j, line.get(i));
 
					ArrayList<Integer> temp = new ArrayList<Integer>(l);
					current.add(temp);
					l.remove(j);
				}
			}
			all_possible = new ArrayList<ArrayList<Integer>>(current);
		}
		int best_score = -6;
		List<Integer> best_lineup = line;
		for (int i=0; i<all_possible.size();i++){
			if (ComputeScore(all_possible.get(i),opponentRound)>best_score){
				best_score = ComputeScore(all_possible.get(i),opponentRound);
				best_lineup = all_possible.get(i);
			}
		}
		//System.out.println(best_score);
		return new Pair(best_score, best_lineup); 
	}

	private int ComputeScore(List<Integer> line1, List<Integer> line2){
		int score = 0;
		for (int i=0; i< line1.size(); i++){
			if (line1.get(i) - line2.get(i) >= 3){
				score += 1;
			}
			if (line2.get(i) - line1.get(i) >= 3){
				score -= 1;
			}
		}
		return score;
	}
	
	private class Pair {
		int key;
		List<Integer> value;
		Pair(int key, List<Integer> value){
			this.key = key;
			this.value = value;
		}
		
		int getKey() {
			return key;
		}
		
		List<Integer> getValue(){
			return value;
		}
	}
	private class PlayRow {
		private List<List<Integer>> opponentRemainDist;
		private int maxScore = -16;
		private int bestLine = -1;
		
		private <E> void swap(List<E> list, int i1, int i2) {
			E temp = list.get(i1);
			list.set(i1, list.get(i2));
			list.set(i2, temp);
		}
		
		private void permuteRow(List<Integer> availableRows, int l, int score) {
			if (l != 0)
				score += permutation(availableRows.get(l - 1), opponentRemainDist.get(l - 1)).getKey();
			if (l == availableRows.size()) {
				//System.out.println("total score:" + score);
				if (score > maxScore) {
					maxScore = score;
					bestLine = availableRows.get(0);
				}
			}
	        else {
	            for (int i = l; i < availableRows.size(); i++) {
	                swap(availableRows, l, i);
	                permuteRow(availableRows, l + 1, score); 
	                swap(availableRows, l, i); 
	            } 
	        } 
	    }
		
		protected Pair useRows(List<Integer> opponentRound){
			// Predict opponent's line distributions
    		Collections.sort(opponentRemainSkills);
			opponentRemainDist = new ArrayList<List<Integer>>();
			opponentRemainDist.add(opponentRound);
			
			for (int i = 0; i < opponentRemainSkills.size(); i += 5) {
				// Prediction policy
				opponentRemainDist.add(new ArrayList<Integer>(
						opponentRemainSkills.subList(i, i + 5)));
			}
			// Finding the best strategy to counter the prediction
			permuteRow(availableRows, 0, 0);
			
			//System.out.println(best_score);
			return new Pair(bestLine, 
					permutation(bestLine, opponentRound).getValue());
		}
	}
	

	@Override
	public List<Integer> playRound(List<Integer> opponentRound) {
		
		List<Integer> round = new ArrayList<Integer>();
		
    	if (state){
    		opponentDistribution.add(new ArrayList<Integer>(opponentRound));
    		for (Integer i : opponentRound) {
    			opponentRemainSkills.remove(i);
    		}
    		Pair temp = new PlayRow().useRows(opponentRound);
    		round = temp.getValue();
    		availableRows.remove((Integer)temp.getKey());
    	}
    	else{
	    	round =	distribution.get(availableRows.get(0));
    		availableRows.remove(0);
            if (opponentRound!=null){
                home_line.add(findVariance(opponentRound)-opponentVar);
            }
    	}
		return round;
	}

	@Override
	public void clear() {
		availableRows.clear();
		for (int i = 0; i < 3; i++)
			availableRows.add(i);
	}

}
