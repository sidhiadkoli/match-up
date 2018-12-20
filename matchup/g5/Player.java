package matchup.g5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.*;
import java.lang.*;

import matchup.sim.utils.*;

public class Player implements matchup.sim.Player {
	private List<Integer> skills;
	private List<List<Integer>> distribution;
	private List<Integer> availableRows;
	private Random rand;
  private List<Integer> opponentSkills;
  private List<List<Integer>> opponentDistribution;

  /* helper variable to pass back results from permutation */
  private List<Integer> permute_result;
  private int best_permuted_score_cur_line;

  /* history variable */
  private List<Game> games;

  private boolean isHome;
  private String prevGameResult;
	private String prevStrategy; //variable to store strategy used in previous game
	private String prevLines; //variable to store previous line strategy
	private double oppAwayDistinct = 1.0;
	private double oppHomeDistinct = 2.0;

    /* created once for repeated games */
	public Player() {
		rand = new Random();
		skills = new ArrayList<Integer>();
		distribution = new ArrayList<List<Integer>>();
		availableRows = new ArrayList<Integer>();
    opponentSkills = new ArrayList<Integer>();
    opponentDistribution = new ArrayList<List<Integer>>();
    games = new ArrayList<Game>();
    isHome = true; // default
    permute_result = new ArrayList<Integer>();
    best_permuted_score_cur_line = -6;
    prevGameResult = "t";

		for (int i=0; i<3; ++i) availableRows.add(i);
	}

  public void init(String opponent) {
  }

    /* called once per game repeat (pair of home/away) */
	public List<Integer> getSkills() {
		//Counter for 10s
		List<Integer> counter10 = new ArrayList<Integer>();
		for(int i=0; i<5; i++){
			counter10.add(9);
			counter10.add(8);
			counter10.add(1);
		}
		//Counter for 7s
		List<Integer> counter7 = new ArrayList<Integer>();
		for(int i=0; i<5; i++){
			counter7.add(10);
			counter7.add(7);
			counter7.add(1);
		}
		// Alternative counter for 7s
		List<Integer> defeat7 = new ArrayList<Integer>();
		for(int i=0; i<5; i++){
			defeat7.add(10);
			defeat7.add(5);
			defeat7.add(3);
		}
		//Aggressive strategy
		List<Integer> aggressive = new ArrayList<Integer>();
		for(int i=0; i<5; i++){
			aggressive.add(9);
			aggressive.add(7);
		}
		for(int i=0; i<3; i++){
			aggressive.add(1);
		}
		aggressive.add(4);
		aggressive.add(3);
		//Defensive strategy
		List<Integer> defensive = new ArrayList<Integer>();
		for(int i=0; i<10; i++){
			defensive.add(7);
		}
		for(int i=0; i<5; i++){
			defensive.add(4);
		}
		// Mixed strategy - counters a lineup of 9s, 8s and 1s
		List<Integer> mixed = new ArrayList<Integer>();
		for(int i=0; i<5; i++){
			mixed.add(8);
			mixed.add(6);
			mixed.add(4);
		}

		//Uses the trueRandom algorithm to generate a balanced random
		//lineup with skills between 4 and 9
		List<Integer> semiRand = trueRandom(4,9,90,15);

    /* obtain and analyze game history */
    games = History.getHistory();

    int ourScore = 0;
    int oppScore = 0;

		//Determine total score of the previous game (adds scores from home and away)
		if(games.size() >= 2){
			for (int i = games.size() - 2; i < games.size(); i++) {
				if(games.get(i).playerA.name.equals("g5")) {
	      	ourScore = ourScore + games.get(i).playerA.score;
	      }
				else {
	        oppScore = oppScore + games.get(i).playerA.score;
	      }
	      if(games.get(i).playerB.name.equals("g5")) {
	        ourScore = ourScore + games.get(i).playerB.score;
	      }
				else {
	        oppScore = oppScore + games.get(i).playerB.score;
	      }
	    }
		}

		//Determine result
		if(ourScore < oppScore){
			prevGameResult = "l";
    }
		else if(ourScore == oppScore){
			prevGameResult = "t";
    }
    else{
      prevGameResult = "w";
    }

    /* determine from history:
     * 1. has opponent skill distribution changed from last game to the game before
     * 2. min/max of player skills from the last distribution
     * 3. avg number of distinct numbers in the past home and away lineups
     */
    Boolean analysisResultUsable = false;
    Boolean g5_playerA = false;
    Boolean g5_playerB = false;
    Boolean oppoSkillChanged = false;
    int oppoMinSkill = 12;
    int oppoMaxSkill = -1;
    double avgNumberHome = -1;
    double avgNumberAway = -1;
    if (games.size() >= 4) {
      analysisResultUsable = true;
      if(games.get(games.size()-1).playerA.name.equals("g5")) {
        g5_playerA = true;
      }
			else {
        g5_playerB = true;
      }

			if (g5_playerA) {
        List<Integer> a = games.get(games.size()-1).playerB.skills;
        List<Integer> b = games.get(games.size()-3).playerB.skills;
				/* 	TEST STATEMENTS
				//System.out.println("====== historical opponent skills distribution ======");
        //System.out.println("Opponent Skills (prev game) = " + a);
        //System.out.println("Opponent Skills (two games before)" + b);
				*/
        Collections.sort(a);
        Collections.sort(b);
        oppoSkillChanged = !a.equals(b);
        for(int i = 0; i < 15; i++) {
          if (games.get(games.size()-1).playerB.skills.get(i) < oppoMinSkill) {
            oppoMinSkill = games.get(games.size()-1).playerB.skills.get(i);
          }
          if (games.get(games.size()-1).playerB.skills.get(i) > oppoMaxSkill) {
            oppoMaxSkill = games.get(games.size()-1).playerB.skills.get(i);
          }
        }
        // avg distinct number for last game (will decide on whether it is home or away)
        List<Integer> dist_number_per_line = new ArrayList<Integer>();
        for(int i = 0; i < 3; i++) {
          dist_number_per_line.add(0);
          Set<Integer> mySet = new HashSet<Integer>();
          for (int j = 0; j < 5; j++) {
            if (!mySet.contains(games.get(games.size() - 1).playerB.distribution.get(i).get(j))){
							dist_number_per_line.set(i, dist_number_per_line.get(i) + 1);
							mySet.add(games.get(games.size() - 1).playerB.distribution.get(i).get(j));
						}
          }
        }
        int sum = 0;
        for (int i = 0; i < dist_number_per_line.size(); i++) {
        	sum = sum + dist_number_per_line.get(i);
        }
        if (games.get(games.size() - 1).playerB.isHome) {
          avgNumberHome = sum / 3;
        }
				else {
          avgNumberAway = sum / 3;
        }

				// avg distinct number for 2nd last game (will decide on whether it is home or away)
        dist_number_per_line = new ArrayList<Integer>();
        for(int i = 0; i < 3; i++) {
            dist_number_per_line.add(0);
            Set<Integer> mySet = new HashSet<Integer>();
            for (int j = 0; j < 5; j++) {
              if (!mySet.contains(games.get(games.size() - 2).playerB.distribution.get(i).get(j))){
								dist_number_per_line.set(i, dist_number_per_line.get(i) + 1);
								mySet.add(games.get(games.size() - 2).playerB.distribution.get(i).get(j));
							}
            }
        }
        sum = 0;
        for (int i = 0; i < dist_number_per_line.size(); i++) {
          sum = sum + dist_number_per_line.get(i);
        }
        if (games.get(games.size() - 2).playerB.isHome) {
          avgNumberHome = sum / 3;
        }
				else {
          avgNumberAway = sum / 3;
        }
      }
			else {
        List<Integer> a = games.get(games.size()-1).playerA.skills;
        List<Integer> b = games.get(games.size()-3).playerA.skills;
        Collections.sort(a);
        Collections.sort(b);
        oppoSkillChanged = !a.equals(b);
        for(int i = 0; i < 15; i++) {
          if (games.get(games.size()-1).playerA.skills.get(i) < oppoMinSkill) {
            oppoMinSkill = games.get(games.size()-1).playerA.skills.get(i);
          }
          if (games.get(games.size()-1).playerA.skills.get(i) > oppoMaxSkill) {
            oppoMaxSkill = games.get(games.size()-1).playerA.skills.get(i);
          }
        }
        // avg distinct number for last game (will decide on whether it is home or away)
        List<Integer> dist_number_per_line = new ArrayList<Integer>();
        for(int i = 0; i < 3; i++) {
          dist_number_per_line.add(0);
          Set<Integer> mySet = new HashSet<Integer>();
          for (int j = 0; j < 5; j++) {
            if (!mySet.contains(games.get(games.size() - 1).playerA.distribution.get(i).get(j))){
							dist_number_per_line.set(i, dist_number_per_line.get(i) + 1);
							mySet.add(games.get(games.size() - 1).playerA.distribution.get(i).get(j));
						}
          }
        }
        int sum = 0;
        for (int i = 0; i < dist_number_per_line.size(); i++) {
          sum = sum + dist_number_per_line.get(i);
        }
        if (games.get(games.size() - 1).playerA.isHome) {
          avgNumberHome = sum / 3;
        }
				else {
          avgNumberAway = sum / 3;
        }
        // avg distinct number for 2nd last game (will decide on whether it is home or away)
        dist_number_per_line = new ArrayList<Integer>();
        for(int i = 0; i < 3; i++) {
          dist_number_per_line.add(0);
          Set<Integer> mySet = new HashSet<Integer>();
          for (int j = 0; j < 5; j++) {
            if (!mySet.contains(games.get(games.size() - 2).playerA.distribution.get(i).get(j))){
							dist_number_per_line.set(i, dist_number_per_line.get(i) + 1);
							mySet.add(games.get(games.size() - 2).playerA.distribution.get(i).get(j));
						}
          }
        }
        sum = 0;
        for (int i = 0; i < dist_number_per_line.size(); i++) {
        	sum = sum + dist_number_per_line.get(i);
        }
        if (games.get(games.size() - 2).playerA.isHome) {
          avgNumberHome = sum / 3;
					this.oppHomeDistinct = avgNumberHome;
        }
				else {
          avgNumberAway = sum / 3;
					this.oppAwayDistinct = avgNumberAway;
        }
      }
    }

		/* Print out results of analysis
    if (analysisResultUsable) {
    	//System.out.println("opponent skills changed? " + oppoSkillChanged);
      //System.out.println("opponent min skill = " + oppoMinSkill);
      //System.out.println("opponent max skill = " + oppoMaxSkill);
      //System.out.println("Avg number of distinct skills per line when home = " + avgNumberHome);
      //System.out.println("Avg number of distinct skills per line when away = " + avgNumberAway);
    }
		else {
      //System.out.println("Not enough games played to generate history analysis.");
    }
		*/

    /* End of analysis */

		/* Tested throwing a random strategy with a certain probability
		 * but did not improve results
		int skip = rand.nextInt(3);
		if(skip == 0){
			int strat = rand.nextInt(6);
			switch(strat){
				case 0:
					this.skills = counter10; this.prevStrategy = "counter10"; break;
				case 1:
					this.skills = counter7; this.prevStrategy = "counter7"; break;
				case 2:
					this.skills = defeat7;  this.prevStrategy = "defeat7"; break;
				case 3:
					this.skills = aggressive; this.prevStrategy = "aggressive"; break;
				case 4:
					this.skills = defensive; this.prevStrategy = "defensive"; break;
				case 5:
					this.skills = mixed; this.prevStrategy = "mixed"; break;
			}
			return this.skills;
		}
		*/

		// Use analysis to throw a counter
		if (analysisResultUsable){
			if(!oppoSkillChanged){
				if(oppoMaxSkill == 11){
					this.skills = counter7;
					this.prevStrategy = "counter7";
				}
				else if(oppoMaxSkill == 10){
					if(oppoMinSkill < 4){
						this.skills = counter10;
						this.prevStrategy = "counter10";
					}
					else{
						this.skills = defeat7;
						this.prevStrategy = "defeat7";
					}
				}
				else if(oppoMaxSkill == 9){
					int guess = rand.nextInt(3);
					switch(guess){
						case 0:
							this.skills = aggressive; this.prevStrategy = "aggressive"; break;
			      case 1:
			        this.skills = defensive; this.prevStrategy = "defensive"; break;
			      case 2:
			        this.skills = mixed; this.prevStrategy = "mixed"; break;
			     }
				}
				else if(oppoMaxSkill == 8){
					this.skills = mixed;
					this.prevStrategy = "mixed";
				}
				else if(oppoMaxSkill <= 7){
					if(oppoMinSkill < 4){
						this.skills = counter7;
						this.prevStrategy = "counter7";
					}
					else{
						this.skills = defeat7;
						this.prevStrategy = "defeat7";
					}
				}
			}
			else{ //opponent skills are changing
				if(prevGameResult == "w" || prevGameResult == "l"){ //throw counter
					if(this.prevStrategy == "defeat7" || this.prevStrategy == "counter7"){
						this.skills = counter10;
						this.prevStrategy = "counter10";
					}
					else if(this.prevStrategy == "defensive"){
						int r = rand.nextInt(2); //
						if (r == 1){
							this.skills = counter7;
							this.prevStrategy = "counter7";
						}
						else{
							this.skills = defeat7;
							this.prevStrategy = "defeat7";
						}
					}
					else if(this.prevStrategy == "counter10"){
						this.skills = defeat7;
						this.prevStrategy = "defeat7";
					}
					else{
						this.skills = defensive;
						this.prevStrategy = "defensive";
					}
				}
				else { //Stay the same, we tied
					switch(prevStrategy){
						case "counter10":
							this.skills = counter10; break;
			      case "counter7":
			        this.skills = counter7; break;
			      case "defeat7":
			        this.skills = defeat7;  break;
						case "aggressive":
				      this.skills = aggressive; break;
						case "defensive":
	 				    this.skills = defensive; break;
						case "mixed":
		 				  this.skills = mixed; break;
						case "semiRand":
		 				  this.skills = semiRand; break;
			     }
				}
			}
		}
		else{
			int choice = rand.nextInt(4);
			switch(choice){
				case 0:
					this.skills = aggressive; this.prevStrategy = "aggressive"; break;
	      case 1:
	        this.skills = defensive; this.prevStrategy = "defensive"; break;
	      case 2:
	        this.skills = mixed; this.prevStrategy = "mixed"; break;
	      case 3:
	        this.skills = semiRand; this.prevStrategy = "semiRand"; break;
	     }
		}
    return this.skills;
  }

	// This algorithm will select 'num' random integers from the range [min, max] that add up to the desired 'sum'.
	// It isn't hard coded to select 15 random numbers adding up to 90, and can be used to adaptively select a team
	// by changing the range or manually selecting a few players and having the algorithm fill out the rest
	public static List<Integer> trueRandom(int min, int max, int sum, int num){
    Random r = new Random();
		int desired_sum = sum;
		int current_sum = 0;
		int remaining = desired_sum - current_sum;
		int current_min = min;
		int current_max = max;
		int num_players = num;
		int player_skill = 0;
    List<Integer> randSkills = new ArrayList<Integer>();

    for(int i=0; i<num; i++){
      num_players--;
      if(num_players != 0){
        while (((remaining - current_max)/num_players) <= min){
          current_max -= 1;
        }
        while (((remaining - current_min)/num_players) >= max){
          current_min += 1;
        }

        int range = current_max - current_min;
        ////System.out.println("The range is: (" + current_min + ", " + current_max + ")");
        if(range <= 0){
          player_skill = current_max;
        }
        else{
          player_skill = current_min + r.nextInt(range);
        }
        randSkills.add(player_skill);
        current_sum += player_skill;
        remaining = desired_sum - current_sum;
      }
      else{
        player_skill = remaining;
        randSkills.add(player_skill);
        current_sum += player_skill;
        remaining = desired_sum - current_sum;
      }
    }
    return randSkills;
  }


	/* three different strategies to divide into lines */
	public List<List<Integer>> rankedLines(List<Integer> skills) {
			Collections.sort(skills);
			distribution = new ArrayList<List<Integer>>();
			int index = 0;
			for(int i = 0; i < 3; i++) {
					List<Integer> line = new ArrayList<Integer>();
					for(int j = 0; j < 5; j++) {
							line.add(skills.get(index));
							index++;
					}
					distribution.add(line);
			}
			return distribution;
	}

	public List<List<Integer>> evenLines(List<Integer> skills)  {
			Collections.sort(skills);
			distribution = new ArrayList<List<Integer>>();
			int i = 0;
			List<Integer> line1 = new ArrayList<Integer>();
			List<Integer> line2 = new ArrayList<Integer>();
			List<Integer> line3 = new ArrayList<Integer>();
			while(i < 15) {
					line1.add(skills.get(i));
					line2.add(skills.get(i+1));
					line3.add(skills.get(i+2));
					i = i + 3;
			}
			distribution.add(line1);
			distribution.add(line2);
			distribution.add(line3);
			return distribution;
	}

	public List<List<Integer>> randLines(List<Integer> skills){
		distribution = new ArrayList<List<Integer>>();
		List<Integer> index = new ArrayList<Integer>();
		for (int i=0; i<15; ++i) index.add(i);

		Collections.shuffle(index);
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


    /* called every home/away switch */
    public List<List<Integer>> getDistribution(List<Integer> opponentSkills, boolean isHome){
    	distribution = new ArrayList<List<Integer>>();
      // update our private variables
      this.isHome = isHome;
      this.opponentSkills = opponentSkills;
			if(isHome){
				if(this.oppAwayDistinct <= 2.0){
					distribution = randLines(this.skills);
				}
				else if(this.oppAwayDistinct <= 3.0){
					distribution = evenLines(this.skills);
				}
				else{
					distribution = rankedLines(this.skills);
				}
			}
			else{
				if(this.oppHomeDistinct <= 2.0){
					distribution = rankedLines(this.skills);
				}
				else if(this.oppHomeDistinct <= 3.0){
					distribution = evenLines(this.skills);
				}
				else{
					distribution = randLines(this.skills);
				}
			}

			// Random strategy choice
			/*
			int choice = rand.nextInt(3);
			switch(choice){
				case 0:
					distribution = rankedLines(this.skills); break;
				case 1:
					distribution = evenLines(this.skills); break;
				case 2:
					distribution = randLines(this.skills); break;
			}
			*/

    	return distribution;
    }




    /* called every round of play
     * when away, opponentRound is historical data
     */
    public List<Integer> playRound(List<Integer> opponentRound) {
        /* initialize return variable */
        List<Integer> round = null;

        /* log opponent data */
        opponentDistribution.add(opponentRound);
        /* permutation when isHome = True */
        if (isHome == true) {
            int selected_line_score = -6;
            int selected_line_index = 0; // default first line, will be overwritten
            for (int i = 0; i < availableRows.size(); i++) {
                best_permuted_score_cur_line = -6; // resets best_permuted_score_cur_line for each line permutation
                permute_result = null;

                line_permute(distribution.get(availableRows.get(i)), opponentRound);

                if (best_permuted_score_cur_line > selected_line_score) {
                    selected_line_score = best_permuted_score_cur_line;
                    selected_line_index = i;
                    round = permute_result;
                } else if (best_permuted_score_cur_line == selected_line_score) {
                    int selected_line_skill_sum = 0;
                    int current_line_skill_sum = 0;
                    for (int j = 0; j < 5; j++) {
                        selected_line_skill_sum = selected_line_skill_sum + round.get(j);
                        current_line_skill_sum = current_line_skill_sum + permute_result.get(j);
                    }
                    if (current_line_skill_sum < selected_line_skill_sum) {
                        /* test */
                        ////System.out.println("2.!!!!!!!!!!!!!!!!!!");

                        selected_line_score = best_permuted_score_cur_line;
                        selected_line_index = i;
                        round = permute_result;
                    }
                } else {}

                /* test */
                ////System.out.println("test: Best permutation of the line: " + permute_result);
                ////System.out.println("test: Resulting net Score of best permutation: " + best_permuted_score_cur_line);
            }
            availableRows.remove(selected_line_index);

            ////System.out.println("Selected Line: " + round);
            ////System.out.println("Resulting net Score: " + selected_line_score);

        } else {

            /* random fillers */
            int n = rand.nextInt(availableRows.size());
            round = new ArrayList<Integer>(distribution.get(availableRows.get(n)));
            availableRows.remove(n);
            Collections.shuffle(round);
        }

    	return round;
    }

    /* permutation function
     * returns ourPoint - opponentPoint under our best permutation
     * permutation algorithm is based on:
     * https://www.geeksforgeeks.org/write-a-c-program-to-print-all-permutations-of-a-given-string/
     */
    private int line_permute(List<Integer> ourTeam, List<Integer> opponent) {
        /*
         * l = starting index of the string
         * r = ending index of the string
         */
        int l = 0;
        int r = ourTeam.size() - 1;
        permute(ourTeam, l, r, opponent);
        return 0;
    }

    private void permute(List<Integer> ourTeam, int l, int r, List<Integer> opponent) {
        if (l == r) {
            //System.out.print(ourTeam);
            int cur_score = 0;
            for (int i = 0; i < ourTeam.size(); i++) {
                if(ourTeam.get(i) - opponent.get(i) >= 3) {
                    cur_score++;
                } else if (opponent.get(i) - ourTeam.get(i) >= 3) {
                    cur_score--;
                } else {}
                ////System.out.println(cur_score); // test
            }
            if (cur_score > best_permuted_score_cur_line) {
                /* test */
                ////System.out.println("!?!?!?!?!?!?!?");

                best_permuted_score_cur_line = cur_score;
                permute_result = new ArrayList<Integer>(ourTeam);
            }
            ////System.out.println("permute_result: " + permute_result);
            ////System.out.println(" : best perm. score = " + cur_score);

        } else {
            for (int i = l; i <= r; i++) {
                /* SWAP */
                int temp = ourTeam.get(l);
                ourTeam.add(l, ourTeam.get(i));
                ourTeam.remove(l + 1);
                ourTeam.add(i, temp);
                ourTeam.remove(i + 1);


                permute(ourTeam, l + 1, r, opponent);

                /* SWAP */
                temp = ourTeam.get(l);
                ourTeam.add(l, ourTeam.get(i));
                ourTeam.remove(l + 1);
                ourTeam.add(i, temp);
                ourTeam.remove(i + 1);
            }
        }
    }


    public void clear() {
    	availableRows.clear();
    	for (int i=0; i<3; ++i) availableRows.add(i);
    }
}
