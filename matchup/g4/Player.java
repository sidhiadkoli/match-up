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

	private boolean isHome;
	private int roundNum;
	
	public Player() {
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
			distribution.add(new Line(Arrays.asList(rows[i])));
		}

		this.isHome = isHome;
		this.roundNum = 0;
	    return distribution;
	}

    public List<Integer> playRound(List<Integer> opponentRound) {
        int idx = isHome ? lineToUse(new Line(opponentRound)) : 0;
		List<Integer> toUse = distribution.get(availableRows.get(idx));

        availableRows.remove(idx);
        this.roundNum++;
		return toUse;
    }

    public void clear() {
    	availableRows.clear();
    	for (int i=0; i<3; ++i) availableRows.add(i);

	    distribution.clear();
    }

    private int lineToUse(Line opponent) {
    	int record = 0;
    	double highScore = Double.MIN_VALUE;

        for (int i = 0; i < availableRows.size(); ++i) {
        	Line curLine = (Line)distribution.get(availableRows.get(i));
        	curLine.permuteFor(opponent);
            double score = curLine.scoreWeighted(opponent);

            if (score > highScore) {
                record = i;
                highScore = score;
            }
        }

        return record;
    }
}
