package matchup.g1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import matchup.sim.utils.*;

public class Player implements matchup.sim.Player {
    private List<Integer> skills;
    private List<Integer> bestTeam;
    private List<Integer> opponentTeam;
    private List<List<Integer>> distribution;
    private boolean globalIsHome;

    private List<Integer> availableRows;

    private Random rand;
    
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
	    skills = new ArrayList<Integer>();
	    
	    Collections.addAll(skills, 1, 1, 9, 9, 8);
	    Collections.addAll(skills, 1, 1, 9, 9, 8);
	    Collections.addAll(skills, 1, 7, 9, 9, 8);
	    
		
		List<Game> games = History.getHistory();
		
		if(games.size() > 1){
			
			List<Integer> opponentPastSkills = buildFromHistory();	//approximates opponent distribution from history
			List<Integer> adaptedPlayersSkills = buildOptimalPlayers(opponentPastSkills);	//generates our lineup of 15 players
			
    		skills = getLowVarianceLineup(adaptedPlayersSkills);	//returns low variance skills
    		//skills = getMixedVarianceLineup(adaptedPlayersSkills); //returns mixed variance skills
		}

	    return skills;
    }
    
    //generate distribution of 15 after learning from history
    public List<Integer> buildFromHistory()
    {
		List<Game> games = History.getHistory();
    	Game game;
		PlayerData opponent;
		PlayerData friendly;
		
		List<Integer> opponentAvgPastSkills = new ArrayList<Integer>();
		Collections.addAll(opponentAvgPastSkills, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
		
		for(int i=0; i<games.size(); i+=2)
		{
			game = games.get(i);
		
			if (game.playerA.name == "g1") {
					friendly = game.playerA;
					opponent = game.playerB;
			} else {
				friendly = game.playerB;
				opponent = game.playerA;
			}
			
			Collections.sort(opponent.skills);
			
			//sum of all previous skills
			for (int j=0; j<15; j++){
				opponentAvgPastSkills.set(j, opponentAvgPastSkills.get(j) + opponent.skills.get(j));
			}
		}
		
		//divide by total games to get adapted skills
		for (int j=0; j<15; j++){
			opponentAvgPastSkills.set(j, (opponentAvgPastSkills.get(j)/(games.size()/2)));
		}
			
		return opponentAvgPastSkills;
    }
    
    //generate our distribution to beat the predicted opponent distribution
    public List<Integer> buildOptimalPlayers(List<Integer> opponentAvgPastSkills)
    {
		List<Integer> friendlyNewSkills = new ArrayList<Integer>();
		Collections.addAll(friendlyNewSkills, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
		
    	for (int j=0; j<15; j++){
			
			if (opponentAvgPastSkills.get(j) <= 6)
				friendlyNewSkills.set(j, opponentAvgPastSkills.get(j) + 3);
			else
				friendlyNewSkills.set(j, opponentAvgPastSkills.get(j) - 2);
		}
	
		int sumOfSkills = 0;
		for(int j=0; j<15; j++)
			sumOfSkills += friendlyNewSkills.get(j);
			
		int diff = 90 - sumOfSkills;
	
		//keep adding randomly till total 90 when sum of skills < 90
		while(diff > 0)
		{
			int x = rand.nextInt(15);
			if (friendlyNewSkills.get(x) < 11)
			{
				friendlyNewSkills.set(x, friendlyNewSkills.get(x) + 1);
				diff--;
			}
		}
	
		//keep subtracting randomly till total 90 when sum of skills > 90
		while(diff < 0)
		{
			int x = rand.nextInt(15);
			if (friendlyNewSkills.get(x) > 1)
			{
				friendlyNewSkills.set(x, friendlyNewSkills.get(x) - 1);
				diff++;
			}
		}
		
		Collections.sort(friendlyNewSkills);
		
		return friendlyNewSkills;
    }
    
    //generate lineup with low variance
    public List<Integer> getLowVarianceLineup(List<Integer> fskills)
    {
    	Collections.sort(fskills);
    	
    	return fskills;
    }
    
    //generate lineup with mixed variance
    public List<Integer> getMixedVarianceLineup(List<Integer> fskills)
    {
    	Collections.sort(fskills);
    	Collections.swap(fskills, 3, 14);
    	Collections.swap(fskills, 4, 13);
    	
    	return fskills;
    }

    public List<List<Integer>> getDistribution(List<Integer> opponentSkills, boolean isHome) {
    	globalIsHome = isHome;
    	List<Integer> index = new ArrayList<Integer>();
    	for (int i=0; i<15; ++i) index.add(i);

    	distribution = new ArrayList<List<Integer>>();

		int n = 0;
    	for (int i=0; i<3; ++i) {
    		List<Integer> row = new ArrayList<Integer>();
    		for (int j=0; j<5; ++j) {
    			row.add(skills.get(index.get(n)));
    			++n;
    		}

    		distribution.add(row);
    	}

    	return distribution;
    }
    
    public int chooseOptimalLineup(List<Integer> opponentRound){
        int lineupCount = availableRows.size();
        int bestLineup = 0;
        int secondBest = 0;
        int bestScore = -6;
        
        if (lineupCount == 3) {
            bestScore = 6;
            for (int i = 0; i < lineupCount; i++) {
                List<Integer> round = new ArrayList<Integer>(distribution.get(availableRows.get(i))); 
                int score = checkLineupScore(round, opponentRound);
                if (score < bestScore) {
                    secondBest = bestLineup;
                    bestLineup = i;
                    bestScore = score;
                }
            }
            return secondBest;
        } else {
            for (int i = 0; i < lineupCount; i++) {
                List<Integer> round = new ArrayList<Integer>(distribution.get(availableRows.get(i))); 
                int score = checkLineupScore(round, opponentRound);
                if (score > bestScore) {
                    bestLineup = i;
                    bestScore = score;
                }
            }
            return bestLineup;
        }
      
    }
 
    public List<Integer> playRound(List<Integer> opponentRound) {

		if (!globalIsHome) {
            int n = rand.nextInt(availableRows.size());
            List<Integer> round = new ArrayList<Integer>(distribution.get(availableRows.get(n)));
            availableRows.remove(n);
           
	    return round;
		}
	
		int n = chooseOptimalLineup(opponentRound);

		List<Integer> round = new ArrayList<Integer>(distribution.get(availableRows.get(n)));
		availableRows.remove(n);

		bestTeam = new ArrayList<Integer>();
		for (int i : round) {
			Collections.addAll(bestTeam, i);
		}

		round = permuteHomeTeam(round, opponentRound);
		return bestTeam;
	
    }

    public void clear() {
    	availableRows.clear();
    	for (int i=0; i<3; ++i) availableRows.add(i);

        // Get history of games.
        List<Game> games = History.getHistory();
		int friendly_away_score = 0;
		int opponent_away_score = 0;
		int friendly_away_wins = 0;
		int opponent_away_wins = 0;
		int friendly_home_score = 0;
		int opponent_home_score = 0;
		int friendly_home_wins = 0;
		int opponent_home_wins = 0;
		int friendly_total_score = 0;
		int opponent_total_score = 0;
		int friendly_total_wins = 0;
		int opponent_total_wins = 0;
		int numGames = 2000;
		//int count_playerA_friendly = 0;
		if (games.size() >= numGames) {
			System.out.println(games.size());
			Game game;
			PlayerData opponent;
			PlayerData friendly;
			for (int i = 0; i < numGames; i++) {
			game = games.get(i);
			if (game.playerA.name == "g1") {
				friendly = game.playerA;
				opponent = game.playerB;
				//count_playerA_friendly += 1;
			} else {
				friendly = game.playerB;
				opponent = game.playerA;
			}
			if (friendly.score > opponent.score) {
				friendly_total_wins += 1;
			} else if (friendly.score < opponent.score) {
				opponent_total_wins += 1;
			}
			friendly_total_score += friendly.score;
			opponent_total_score += opponent.score;
			if (friendly.isHome) {
				if (friendly.score > opponent.score) {
				friendly_home_wins += 1;
				} else if (friendly.score < opponent.score) {
				opponent_home_wins += 1;
				}
				friendly_home_score += friendly.score;
				opponent_home_score += opponent.score;
			}
			if (!friendly.isHome) {
				if (friendly.score > opponent.score) {
				friendly_away_wins += 1;
				} else if (friendly.score < opponent.score) {
				opponent_away_wins += 1;
				}
				friendly_away_score += friendly.score;
				opponent_away_score += opponent.score;
			}
			}
			
			
			System.out.println("end result:");

			System.out.println("home:");
			System.out.println("friendly result:");
			System.out.println("score:");
			System.out.println(friendly_home_score);
			System.out.println("wins:");
			System.out.println(friendly_home_wins);
			System.out.println("opponent result:");
			System.out.println("score:");
			System.out.println(opponent_home_score);
			System.out.println("wins:");
			System.out.println(opponent_home_wins);
			System.out.println("away:");
			System.out.println("friendly result:");
			System.out.println("score:");
			System.out.println(friendly_away_score);
			System.out.println("wins:");
			System.out.println(friendly_away_wins);
			System.out.println("opponent result:");
			System.out.println("score:");
			System.out.println(opponent_away_score);
			System.out.println("wins:");
			System.out.println(opponent_away_wins);
			System.out.println("friendly result:");
			//System.out.println("count_playerA_friendly:");
			//System.out.println(count_playerA_friendly);
			System.out.println("score:");
			System.out.println(friendly_total_score);
			System.out.println("wins:");
			System.out.println(friendly_total_wins);
			System.out.println("opponent result:");
			System.out.println("score:");
			System.out.println(opponent_total_score);
			System.out.println("wins:");
			System.out.println(opponent_total_wins);
		}
    }

    public List<Integer> permuteHomeTeam(List<Integer> homeTeam, List<Integer> awayTeam){
        if(checkLineupScore(homeTeam, awayTeam) == 5)
            return bestTeam;
            
		permute(homeTeam, awayTeam);
        return bestTeam;
    }

    public void permute(List<Integer> arr, List<Integer> away){
        permuteHelper(arr, 0, away);
    }

    public void permuteHelper(List<Integer> arr, int index, List<Integer> away){
        for(int i = index; i < arr.size(); i++){
            if(checkLineupScore(arr, away) > checkLineupScore(bestTeam, away)){
                for(int j=0; j < arr.size();j++){
                    bestTeam.set(j, arr.get(j));
                }
            }
            Collections.swap(arr, index, i);
            permuteHelper(arr, index+1, away);
            Collections.swap(arr, index, i);
        }
    }

    public static int checkLineupScore(List<Integer> homeTeam, List<Integer> awayTeam){
        int score = 0;
        for(int i = 0; i < homeTeam.size(); i++){
            if(homeTeam.get(i) > awayTeam.get(i) + 2){
                score++;
            } else if(awayTeam.get(i) > homeTeam.get(i) + 2) {
                score--;
            } else if (homeTeam.get(i) == awayTeam.get(i) - 2) {
			score++;
	    	} else if (awayTeam.get(i) == homeTeam.get(i) - 2) {
			score--;
	    	}
        }
        
        return score;
    }
}
