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
	    return distribution;
	}

    public List<Integer> playRound(List<Integer> opponentRound) {
    	//int n = rand.nextInt(availableRows.size());    	

    	List<Integer> curRow = distribution.get(availableRows.get(0));
		availableRows.remove(0);

		// for (int elt : curRow) {
  //   		System.out.print(elt + " ");
  //   	}

		if (isHome) {
			this.maxScore = Integer.MIN_VALUE;
			this.maxPermute = new ArrayList<Integer>(Arrays.asList(0,0,0,0,0));
			findOptimalPermutation(new ArrayList<Integer>(), curRow, opponentRound);
			return maxPermute;
		}

		return curRow;

		//List<Integer> round = ArrayList<Integer>();

    	//Collections.shuffle(round);

    	
    }

    public void clear() {
    	availableRows.clear();
    	for (int i=0; i<3; ++i) availableRows.add(i);

	    distribution.clear();
    }

    private int calculateScore(List<Integer> myLine, List<Integer> opponentLine) {
        int score = 0;

        for (int i = 0; i < myLine.size(); ++i) {
        	int diff = myLine.get(i) - opponentLine.get(i);
            if (diff >= 3) { ++score; }
            else if (diff <= -3) { --score; } 
        }

        return score;
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
	            //System.out.print("Hi");

	            tMyList.add(myLine.get(i));
	            tMyLine.remove(i);

	            //for (int elt : tMyList) {System.out.print(elt + " "); }
	            //System.out.print('\n');
	            //for (int elt : tMyLine) {System.out.print(elt + " "); }

	            findOptimalPermutation(tMyList, tMyLine, opponentList);
	        }        
        }
    }
}
