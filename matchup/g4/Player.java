package matchup.g4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Arrays;

public class Player implements matchup.sim.Player {
	private List<Integer> skills;
	private List<List<Integer>> distribution;
	private List<Integer> availableRows;

	private int maxScore;
	private List<Integer> maxPermute;

	private Random rand;

	private boolean isHome;
	private int roundNum;
	
	public Player() {
		rand = new Random();
		skills = new ArrayList<Integer>();
		distribution = new ArrayList<List<Integer>>();
		availableRows = new ArrayList<Integer>();

		for (int i=0; i<3; ++i) availableRows.add(i);
	}
	
    public void init(String opponent) {
    }

    public List<Integer> getSkills() {
		Integer s[] = {9,9,9,9,9,8,8,8,8,8,1,1,1,1,1};

		skills = new ArrayList<Integer>(Arrays.asList(s));

		return skills;
    }

    public List<List<Integer>> getDistribution(List<Integer> opponentSkills, boolean isHome) {
	    Integer rows[][];

        if (isHome) {
		    rows = new Integer[][] {{9,9,8,8,1},{9,9,8,1,1},{9,8,8,1,1}};
	    } else {
		    rows = new Integer[][] {{9,9,9,9,9},{8,8,8,8,8},{1,1,1,1,1}};
	    }
	
		for (int i = 0; i < 3; ++i) {
			distribution.add(new ArrayList<Integer>(Arrays.asList(rows[i])));
		}

		this.isHome = isHome;
		this.roundNum = 0;
	    return distribution;
	}

    public List<Integer> playRound(List<Integer> opponentRound) {
		if (isHome) {
            availableRows.remove(lineToUse(opponentRound));
			return maxPermute;
		} else {
			List<Integer> toUse = distribution.get(availableRows.get(0));
			availableRows.remove(0);
			return toUse;
		}
    }

    public void clear() {
    	availableRows.clear();
    	for (int i=0; i<3; ++i) availableRows.add(i);

	    distribution.clear();
    }

    private int lineToUse(List<Integer> opponentLine) {
    	this.maxScore = Integer.MIN_VALUE;
    	this.maxPermute = new ArrayList<Integer>(Arrays.asList(0,0,0,0,0));
    	
    	List<Integer> scores = new ArrayList<Integer>(3);
    	List<List<Integer>> permutes = new ArrayList<List<Integer>>(3); 

    	for (int rowNum : availableRows) {
            List<Integer> curLine = distribution.get(rowNum);
            findOptimalPermutation(new ArrayList<Integer>(), curLine, opponentLine);
            scores.add(this.maxScore);
            permutes.add(new ArrayList(this.maxPermute));
    	}

        int bestLine = 0;
        double bestScore = Double.MIN_VALUE;

    	for (int i = 0; i < availableRows.size(); ++i) {
            List<Integer> curLine = permutes.get(i);

            int sum = 0;
            for (int elt: curLine) {sum += elt;}
            double score = scores.get(i) / (double)sum;

            if(score > bestScore) {
            	bestScore = score;
            	bestLine = i;
            	this.maxPermute = permutes.get(i);
            }
    	}

    	return bestLine;
    }

    private int calculateScore(List<Integer> myLine, List<Integer> opponentLine) {
        int tally = 0;

        for (int i = 0; i < myLine.size(); ++i) {
        	int diff = myLine.get(i) - opponentLine.get(i);
            if (diff >= 3) { ++tally; }
            else if (diff <= -3) { --tally; } 
        }

        return tally;
    }

    private void findOptimalPermutation(List<Integer> myList, List<Integer> myLine, List<Integer> opponentList) {
        if (myLine.size() == 0) {
        	int score = calculateScore(myList, opponentList);
        	if (score > this.maxScore) {
        		this.maxScore = score;
        		this.maxPermute = myList;
        	}
        } else {
        	for (int i = 0; i < myLine.size(); ++i) {
	            List<Integer> tMyList = new ArrayList<Integer>(myList);
	            List<Integer> tMyLine = new ArrayList<Integer>(myLine);

	            tMyList.add(myLine.get(i));
	            tMyLine.remove(i);
	            findOptimalPermutation(tMyList, tMyLine, opponentList);
	        }
        }
    }
}
