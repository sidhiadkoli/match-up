package matchup.g10;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.lang.Math;
import java.util.Map;
import java.util.HashMap;



public class Player implements matchup.sim.Player {

        public class Skill {
	    private Integer value;
	    private List<Integer> preferenceList;
	    private List<Skill> manMarking;
	    private Skill partner;
	    private List<Skill> offers;
	    private Integer proposals;
    
	    public Skill(int v, List<Integer> l){
	        value = v;
	        preferenceList = l;
		offers = new ArrayList<Skill>();
		proposals = 0;
	    }

	    public List<Integer> preferenceList(){
		return preferenceList;
	    }

	    public void buildMM(List<Skill> oppSkills){
		//build preference list against a specific team instead of the generic one
		manMarking = new ArrayList<Skill>();
		for (Integer p : preferenceList){
		    for (Skill opp : oppSkills){
			if (opp.getValue() == p){
			    manMarking.add(opp);
			}
		    }
		}
		proposals = 0;
	    }
	    
	    
	    public void propose(){
		//proposes to next availible skill on updated preference list
		manMarking.get(proposals).addProp(this);
		proposals ++;
	    }

	    public void addProp(Skill opp){
		offers.add(opp);
	    }

	    public void update(){
		//checks proposals and takes best one
		for(Integer p: preferenceList){
		    if(partner != null && partner.getValue() == p){
			return;
		    }
		    for(Skill opp: offers){
			if(opp.getValue() == p){
			    if(partner != null){
				partner.rejection();
			    }
			    partner = opp;
			    opp.setPartner(this);
			    offers.clear();
			    return;
			}
		    }
		}
	    }

	    public void clear(){
		partner = null;
		offers = new ArrayList<Skill>();
		proposals = 0;
	    }

	    public void rejection(){
		//resets partner to -1 if left in update
		partner = null;
	    }

	    public void setPartner(Skill opp){
		partner = opp;
	    }
	    
	    public Skill getPartner(){
		return partner;
	    }

	    public Integer getValue(){
		return value;
	    }
	}

    public class Line {

	private List<Skill> members;
	private List<Integer> values;
	
	public Line(List<Integer> players, boolean ourTeam){
	    values = new ArrayList<Integer>();
	    members = new ArrayList<Skill>();
	    for(int p : players){
		values.add(p);
		Skill member;
		if(ourTeam) {
		    member = new Skill(p, prefLists.get(p-1));
		} else {
		    member = new Skill(p, opponentPrefLists.get(p-1));
		}
		members.add(member);
	    }    
	}

	public Integer score(){
	    //lower score means better matchups
	    Integer score = 0;
	    for(Skill m : members){
		score += (m.preferenceList().indexOf(m.getPartner().getValue()));
	    }
	    return score;
	}

	public Integer score(List<Integer> subset){
	    //scores part of a line
	    Integer score = 0;
	    for(int s : subset){
		for(Skill m: members){
		    if(m.getValue() == s){
			score += (m.preferenceList().indexOf(m.getPartner().getValue()));
		    }
		}
	    }
	    return score;
	}

	public Integer size(){
	    return members.size();
	}

	public Integer marriages(){
	    //return number of engaged players in line
	    int count = 0;
	    for(Skill s: members){
		if(s.getPartner() != null){
		    count ++;
		}
	    }
	    return count;
	}

	public void clear(){
	    for(Skill s: members){
		s.clear();
	    }
	}

	public List<Skill> unmarried(){
	    //returns unmarried skills
	    List<Skill> bachlors = new ArrayList<Skill>();
	    for(Skill s: members){
		if(s.getPartner() == null){
		    bachlors.add(s);
		}
	    }
	    return bachlors;
	}

	public void adjustPL(Line opp){
	    //make member preference lists fit the opponents team comp
	    for(Skill m: members){
		m.buildMM(opp.memberList());
	    }
	}

	public List<Skill> memberList(){
	    return members;
	}

	public List<Integer> valueList(){
	    return values;
	}

	public void removePlayers(List<Integer> played){
	    //take player off the line once they have been played
	    for (int p: played){
		for (int i=members.size()-1; i>=0; i--){
		    if(members.get(i).getValue() == p){
			members.remove(i);
			values.remove(i);
			break;
		    }
		}
	    }
	}
    }

    

        private List<Integer> skills;
	private List<List<Integer>> distribution;

	private List<Integer> availableRows;

	private Random rand;

	private boolean home;

	private List<Integer> bestLine = new ArrayList<Integer>();
	private int score; 
	private int counter;
        private List<Integer> strategy = Arrays.asList(-3,2,10,9,8,7,6,-4,1,5,0,-5,-1,4,-6,-7,-8,-9,-10,-2,3);
        private ArrayList<ArrayList<Integer>> prefLists;
        private ArrayList<ArrayList<Integer>> opponentPrefLists;
        private Line lineup;
        private Line opponentLineup;

    
    
	public Player() {
		rand = new Random();
		skills = new ArrayList<Integer>();
		distribution = new ArrayList<List<Integer>>();
		availableRows = new ArrayList<Integer>();

		for (int i = 0; i < 3; ++i)
			availableRows.add(i);

		prefLists = new ArrayList<ArrayList<Integer>>();
		opponentPrefLists = new ArrayList<ArrayList<Integer>>();
		
	        for (int i=1; i<12; i++) {
		        ArrayList<Integer> ourList = new ArrayList<Integer>();
			ArrayList<Integer> theirList = new ArrayList<Integer>();
			for (int s : strategy) {
			    if (i+s >= 1){ if (i+s <= 11){
				    ourList.add(i+s); }}
			    
			    if (i-s >= 1){ if (i-s <= 11){
				    theirList.add(i-s); }}
			}
			prefLists.add(ourList);
			opponentPrefLists.add(theirList);
		}
		
	}

	public void init(String opponent) {
	}

	// NINE 9s one 4 five 1s
	public List<Integer> getSkills() {	    
	    skills = new ArrayList<Integer>();
	    /*	    skills.add(4); // adding one 4
	    for (int i = 0 ; i < 9; i++){
		
		//adding nine 9s
		skills.add(9);
		
		//adding five 1s
		if(i%2 == 0){
		    skills.add(1);
		}
		} */
	    for (int i=0; i<6; i++){
		int x = rand.nextInt(11) + 1;
		skills.add(x);
		skills.add(12-x);
	    }
	    skills.add(6);
	    skills.add(7);
	    skills.add(5);
	    System.out.println("returned skills");
	    return skills;
	}

        public List<Integer> GaleShapely(Line ourTeam, Line theirTeam){
	    ourTeam.adjustPL(theirTeam);
	    while (theirTeam.marriages() < theirTeam.size()){
		for( Skill member : ourTeam.unmarried() ){
		    member.propose();
		}
		for( Skill member : theirTeam.memberList() ){
		    member.update();
		}
	    }
	    ArrayList<Integer> ordering = new ArrayList<Integer>();
	    for( Skill member : theirTeam.memberList() ){
		ordering.add(member.getPartner().getValue());
	    }
	    return ordering;
        }
    
	// -- Gather information about opponent's skills --
	private Map<String, Double> getSkillStats(List<Integer> skills) {

		Map<String, Double> stats = new HashMap<String, Double>();

		stats.put("mean", 6.0);

		skills.sort(null);

		// Min and max
		double min, max;
		min = skills.get(0);
		max = skills.get(14);
		stats.put("min", min);
		stats.put("max", max);

		// Range
		double range = max - min;
		stats.put("range", range);

		// Opponent standard deviation
		double stdev;
		double sqr_sum = 0;
		for (int s : skills) {
			sqr_sum += Math.pow((s-6), 2);
		}
		stdev = Math.sqrt(sqr_sum/14);
		stats.put("stdev", stdev);

		return stats;
	}

	// -- Get a count of all skills present --
	private Map<Integer, Integer> getSkillCount(List<Integer> skills) {

		Map<Integer, Integer> skillCount = new HashMap<Integer, Integer>();
		for (int s : skills) {
			if (!skillCount.containsKey(s))
				skillCount.put(s, 1);
			else
				skillCount.replace(s, skillCount.get(s)+1);
		}

		return skillCount;
	}

	
	// -- Map one set of skills to another set of skills that according to win, tie, lose outcome
	private Map<String, Map<Integer, List<Integer>>> getSkillMapping(List<Integer> baseSkills, List<Integer> oppSkills) {
		/*
		* Both skill parameters need to be collections of unique skills e.g. pass in opp_skill_count.keySet()
		* baseSkills: these are the skills that will be they keys of the map
		* oppSkills: these are the skills that will be the values mapped to keys of the map
		*/

		Map<Integer, List<Integer>> win = new HashMap<Integer, List<Integer>>();
		Map<Integer, List<Integer>> tie = new HashMap<Integer, List<Integer>>();
		Map<Integer, List<Integer>> lose = new HashMap<Integer, List<Integer>>();

		for (int base_s : baseSkills) {
			
			List<Integer> val_win = new ArrayList<Integer>();
			List<Integer> val_tie = new ArrayList<Integer>();
			List<Integer> val_lose = new ArrayList<Integer>();
			
			for (int opp_s : oppSkills) {
				if (base_s - opp_s >= 3) {
					val_win.add(opp_s);
				} else if (Math.abs(base_s - opp_s) <=2) {
					val_tie.add(opp_s);
				} else if (base_s - opp_s <= -3) {
					val_lose.add(opp_s);
				}
			}

			win.put(base_s, val_win);
			tie.put(base_s, val_tie);
			lose.put(base_s, val_lose);

		}

		Map<String, Map<Integer, List<Integer>>> mapping = new HashMap<String, Map<Integer, List<Integer>>>();
		mapping.put("wins_against", win);
		mapping.put("ties_against", tie);
		mapping.put("loses_against", lose);

		return mapping;

	
	}

	public List<List<Integer>> getDistribution(List<Integer> opponentSkills, boolean isHome) {
		distribution = new ArrayList<List<Integer>>();
		home = isHome;
		opponentLineup = new Line(opponentSkills, false);
		lineup = new Line(skills, true);
		
		skills.sort(null);
		opponentSkills.sort(null);
	       
		System.out.println("our skills: " + skills);
		System.out.println("opponent skills: " + opponentSkills);

		if (isHome) {
			// -- Arrange rows to be optimal for HOME play --

			// get stats on our skills
			Map<String, Double> ourStats = getSkillStats(skills);
			//System.out.println("ourStats: " + ourStats);

			// get our skill count
			Map<Integer, Integer> selfSkillCount = getSkillCount(skills);
			//System.out.println("selfSkillCount: " + selfSkillCount);

			// get stats on opponent's skills
			Map<String, Double> oppStats = getSkillStats(opponentSkills);
			//System.out.println("oppStats: " + oppStats);

			// get opponent's skill count
			Map<Integer, Integer> oppSkillCount = getSkillCount(opponentSkills);
			//System.out.println("oppSkillCount: " + oppSkillCount);

			// get our skill mapping: for each of our skills find out which of the opponent's skills it will beat, tie to, lose against
			Map<String, Map<Integer, List<Integer>>> selfSkillMapping = getSkillMapping(skills, opponentSkills);
			//System.out.println("selfSkillMapping: " + selfSkillMapping);

			// get opponent's sill mapping: for each of opponent's skills find out which of our skills it will beat, tie to, lose against
			Map<String, Map<Integer, List<Integer>>> oppSkillMapping = getSkillMapping(opponentSkills, skills);
			//System.out.println("oppSkillMapping: " + oppSkillMapping);


			/*// >> Split lines differently depending on opponent skill count
			if (oppSkillCount.values().equals(Arrays.asList(5, 5, 5))) {
				System.out.println("! opponent has three values repeated 5 times each!");
			} else if (Collections.max(oppSkillCount.values()) > 6) {
				System.out.println("opponent has one value repeated over 7 times");
			} else {
				System.out.println("no specific opponent skill distribution");
			}*/

			// get the skills that win against at least 1 thing (but then this can be leveraged with counts)
			Map<Integer, List<Integer>> win_skills = new HashMap<Integer, List<Integer>>();
			int win_skills_count = 0; // how many skills (incl repetitions) we have that win against at least 1 of opp skills
			List<Integer> wins = new ArrayList<Integer>();
			for (Integer self_s : selfSkillMapping.get("wins_against").keySet()) {
				List<Integer> counts = new ArrayList<Integer>(); // count of how many we have, count of how may opp skills it wins against
				if (selfSkillMapping.get("wins_against").get(self_s).size() > 0) {
					counts.add(selfSkillCount.get(self_s));
					win_skills_count += selfSkillCount.get(self_s);
					counts.add(selfSkillMapping.get("wins_against").get(self_s).size());
					if (!wins.contains(selfSkillMapping.get("wins_against").get(self_s).size()))
						wins.add(selfSkillMapping.get("wins_against").get(self_s).size());
					win_skills.put(self_s, counts);
				}
			}
			wins.sort(null);
			Collections.reverse(wins);
			//System.out.println("win_skills: " + win_skills);
			//System.out.println("win_skills_count: " + win_skills_count);
			//System.out.println("wins: " + wins);

			// -- Distribute win skills into 3 lines --
			for (int c=0; c<3; c++)
				distribution.add(new ArrayList<Integer>());

			for (int win_count : wins) {
				for (int win_skill : win_skills.keySet()) {
					if (win_skills.get(win_skill).get(1) == win_count) {
						int i=0;
						while (i<win_skills.get(win_skill).get(0)) {
							if (distribution.get(i%3).size() < 5) {
								distribution.get(i%3).add(win_skill);
								i++;
							}
						}
					}
				}
			}
			//System.out.println("distribution after distributing win_skills: " + distribution);


			// get the skills that tie against at least 1 thing (but then this can be leveraged with counts)
			// first we need to remove any of the win skills we've already distributed
			for (int win_skill : win_skills.keySet())
				selfSkillMapping.get("ties_against").remove(win_skill);
			//System.out.println("new ties_against: " + selfSkillMapping.get("ties_against"));
			
			Map<Integer, List<Integer>> tie_skills = new HashMap<Integer, List<Integer>>();
			if (!selfSkillMapping.get("ties_against").isEmpty()) {
				int tie_skills_count = 0; // how many skills (incl repetitions) we have that tie against at least 1 of opp skills
				List<Integer> ties = new ArrayList<Integer>();
				for (Integer self_s : selfSkillMapping.get("ties_against").keySet()) {
					List<Integer> counts = new ArrayList<Integer>(); // count of how many we have, count of how may opp skills it ties against
					if (selfSkillMapping.get("ties_against").get(self_s).size() > 0) {
						counts.add(selfSkillCount.get(self_s));
						tie_skills_count += selfSkillCount.get(self_s);
						counts.add(selfSkillMapping.get("ties_against").get(self_s).size());
						if (!ties.contains(selfSkillMapping.get("ties_against").get(self_s).size()))
							ties.add(selfSkillMapping.get("ties_against").get(self_s).size());
						tie_skills.put(self_s, counts);
					}
				}
				ties.sort(null);
				Collections.reverse(ties);
				//System.out.println("tie_skills: " + tie_skills);
				//System.out.println("tie_skills_count: " + tie_skills_count);
				//System.out.println("ties: " + ties);

				// -- Distribute tie skills into 3 lines --
				for (int tie_count : ties) {
					//System.out.println("tie count: " + tie_count);
					for (int tie_skill : tie_skills.keySet()) {
						if (tie_skills.get(tie_skill).get(1) == tie_count) {
							//System.out.println("tie skill with matching count: " + tie_skill);
							int i=0;
							int added=0;
							while (added != tie_skills.get(tie_skill).get(0)) {
								if (distribution.get(i%3).size() < 5) {
									distribution.get(i%3).add(tie_skill);
									i++;
									added++;
								} else {
									i++;
								}
							}
						}
					}
				}
				//System.out.println("distribution after distributing tie_skills: " + distribution);
			}

			// get the skills that lose against at least 1 thing (but then this can be leveraged with counts)
			// first we need to remove any of the win and tie skills we've already distributed
			for (int win_skill : win_skills.keySet())
				selfSkillMapping.get("loses_against").remove(win_skill);
			for (int tie_skill : tie_skills.keySet())
				selfSkillMapping.get("loses_against").remove(tie_skill);
			//System.out.println("new loses_against: " + selfSkillMapping.get("loses_against"));
			
			Map<Integer, List<Integer>> lose_skills = new HashMap<Integer, List<Integer>>();
			if (!selfSkillMapping.get("loses_against").isEmpty()) {
				int lose_skills_count = 0; // how many skills (incl repetitions) we have that lose against at least 1 of opp skills
				List<Integer> losses = new ArrayList<Integer>();
				for (Integer self_s : selfSkillMapping.get("loses_against").keySet()) {
					List<Integer> counts = new ArrayList<Integer>(); // count of how many we have, count of how may opp skills it loses against
					if (selfSkillMapping.get("loses_against").get(self_s).size() > 0) {
						counts.add(selfSkillCount.get(self_s));
						lose_skills_count += selfSkillCount.get(self_s);
						counts.add(selfSkillMapping.get("loses_against").get(self_s).size());
						if (!losses.contains(selfSkillMapping.get("loses_against").get(self_s).size()))
							losses.add(selfSkillMapping.get("loses_against").get(self_s).size());
						lose_skills.put(self_s, counts);
					}
				}
				losses.sort(null);
				Collections.reverse(losses);
				//System.out.println("lose_skills: " + lose_skills);
				//System.out.println("lose_skills_count: " + lose_skills_count);
				//System.out.println("losses: " + losses);

				// -- Distribute tie skills into 3 lines --
				for (int lose_count : losses) {
					//System.out.println("lose count: " + lose_count);
					for (int lose_skill : lose_skills.keySet()) {
						if (lose_skills.get(lose_skill).get(1) == lose_count) {
							//System.out.println("lose skill with matching count: " + lose_skill);
							int i=0;
							int added=0;
							while (added != lose_skills.get(lose_skill).get(0)) {
								if (distribution.get(i%3).size() < 5) {
									distribution.get(i%3).add(lose_skill);
									i++;
									added++;
								} else {
									i++;
								}
							}
						}
					}
				}
				//System.out.println("distribution after distributing lose_skills: " + distribution);
			}

			
			

		/*if (isHome) {
			// arrange rows to be optimal for HOME play
			// System.out.println("HOME play"); //

			List<Integer> leftover = new ArrayList<Integer>();

			for (int i = 0; i < 3; ++i) {
				List<Integer> row = new ArrayList<Integer>();
				List<Integer> indices = new ArrayList<Integer>(
						Arrays.asList(i, i + 3, i + 6, (14 - (i + 3)), (14 - i)));
				// System.out.println("row " + i + ": " + indices + " (indices)"); //

				for (int ix : indices) {
					if (!row.contains(skills_L.get(ix)))
						row.add(skills_L.get(ix));
					else
						leftover.add(skills_L.get(ix));
				}

				// System.out.println("row " + i + ": " + row + " (values)");
				distribution.add(row);
			}

			// System.out.println("skills leftover: " + leftover);
			// System.out.println("distributions: " + distribution.get(0) + ", " +
			// distribution.get(1) + ", " + distribution.get(2));

			for (int s : leftover) {
				boolean added = false;
				for (int i = 0; i < 3; ++i) {
					if ((distribution.get(i).size() < 5) && !(distribution.get(i).contains(s))) {
						distribution.get(i).add(s);
						added = true;
					} else {
						continue;
					}
				}
				if (!added) {
					for (int i = 0; i < 3; ++i) {
						if (distribution.get(i).size() < 5)
							distribution.get(i).add(s);
					}
				}
			}*/

			// System.out.println("distributions: " + distribution.get(0) + ", " +
			// distribution.get(1) + ", " + distribution.get(2));

		} else {
			// arrange rows to be optimal for AWAY play
			// System.out.println("AWAY play");

			List<Integer> row1, row2, row3;

			row1 = new ArrayList<Integer>(Arrays.asList(skills.get(14), skills.get(13), skills.get(12), skills.get(3), skills.get(11)));
			row2 = new ArrayList<Integer>(Arrays.asList(skills.get(0), skills.get(1), skills.get(2), skills.get(4), skills.get(10)));
			row3 = new ArrayList<Integer>(Arrays.asList(skills.get(5), skills.get(6), skills.get(7), skills.get(8), skills.get(9)));

			distribution.add(row1);
			distribution.add(row2);
			distribution.add(row3);
		}

		System.out.println("distributions: " + distribution.get(0) + ", " +
		distribution.get(1) + ", " + distribution.get(2));

		return distribution;
	}

	public List<Integer> playRound(List<Integer> opponentRound) {

  	        Integer n;
		if (availableRows.size() == 1){
		        n = availableRows.get(0);
		} else if (!home) {
		        n = selectAwayLine(opponentRound);
		} else {
		        n = selectHomeLine(opponentRound);
		}
		System.out.println("selected line: " + n);
		availableRows.remove(n);

		List<Integer> round = distribution.get(n);
 
		if (home) {
			round = bestPermutation(round, opponentRound);
		}
		
		lineup.removePlayers(round);
		return round;
	}
    
        public void clear() {
		availableRows.clear();
		for (int i = 0; i < 3; ++i)
		    availableRows.add(i);
	}

	// This selects the best list to play for each round and returns its index in
	// availableRows

	public Integer selectAwayLine(List<Integer> opponentRound) {

	    if (availableRows.size() != 3){
		opponentLineup.removePlayers(opponentRound);
	    }

	    Line goodOpp = new Line(opponentLineup.valueList(), true);
	    Line badHome = new Line(lineup.valueList(), false);
	    
	    GaleShapely(goodOpp, badHome);
	    
	    List<Integer> lineSkill = Arrays.asList(0, 0, 0);

	    for (Integer i : availableRows) {
		lineSkill.set(i, badHome.score(distribution.get(i)));
	    }

	    int maxScore = 0;
	    for( int score : lineSkill ){
		if (score > maxScore){
		    maxScore = score;
		}
	    }
	    return lineSkill.indexOf(maxScore);
	}

	public Integer selectHomeLine(List<Integer> opponentRound) {

	    opponentLineup.removePlayers(opponentRound);
	    
	    if (availableRows.size() == 2){

		
		List<Integer> opponentNextRound = opponentLineup.valueList();

		int chooseFirst = totalLineWins(distribution.get(availableRows.get(0)), opponentRound) + totalLineWins(distribution.get(availableRows.get(1)), opponentNextRound);
		int chooseSecond = totalLineWins(distribution.get(availableRows.get(0)), opponentNextRound) + totalLineWins(distribution.get(availableRows.get(1)), opponentRound);

		if(chooseFirst > chooseSecond){
		    System.out.println(chooseFirst + "wins with first");
		    return availableRows.get(0);
		} else {
		    System.out.println(chooseSecond + "wins with second");
		    return availableRows.get(1);
		}
	    } else { //selecting first home line

		List<Integer> opponentRemaining = opponentLineup.valueList();
		List<Integer> lineScore = Arrays.asList(0, 0, 0);

		System.out.println("Opponents remaining: " + opponentRemaining);
		
		for (Integer i : availableRows) { // 0-2
		    Line temp = new Line(skills, true);
		    temp.removePlayers(distribution.get(i));

		    opponentLineup.clear();
		    GaleShapely(temp, opponentLineup);
		    
		    lineScore.set(i, totalLineWins(distribution.get(i), opponentRound)+temp.score());
		}
		System.out.println("linescores: " + lineScore);

		int maxScore = 0;
		for(int i: lineScore){
		    if (i > 0){
			maxScore = i;
		    }
		}
		return lineScore.indexOf(maxScore);
	    }
	}
	

    //        public Integer totalLineSkill(List<Integer> line) {
    //		int skillLevel = 0;

    ///		for (Integer player : line) {
    //			skillLevel += player;
    //		}

    //  //		return skillLevel;
    //	}
//
	public Integer totalLineWins(List<Integer> line, List<Integer> opponentLine) {
		line = bestPermutation(line,  opponentLine);
	       	int rowWins = 0;
	       	
	       	for(int j=0; j<line.size(); j++){
	       		if (line.get(j)-opponentLine.get(j) > 2) rowWins++;
	       		if (line.get(j)-opponentLine.get(j) < -2) rowWins--;
	       	}
	       	return rowWins;
    	}

    	public void permute(List<Integer> line, int j, List<Integer> opponentLine){ 
	        for(int i = j; i < line.size(); i++){
	            java.util.Collections.swap(line, i, j);
	            permute(line, j+1, opponentLine); 
	            java.util.Collections.swap(line, j, i);
	        }

	        if(j == line.size() -1){
	            counter++; 
	            //System.out.println(counter + java.util.Arrays.toString(line.toArray())); 
	            int temp = compareLine(line, opponentLine); 

	            if(temp > score){ 
	                score = temp; 
	                //System.out.println("I just set the score: " + score); 
	                bestLine.clear(); 
	                bestLine.addAll(line); 
	                //System.out.println("I just set best line: " + bestLine); 
	            }
	        }
	}

	//System.out.println("This is the best line end of permute: " + bestLine); 
	 

	//figure out which of two lines win 
    	public int compareLine(List<Integer> home, List<Integer> away){
		int homeScore = 0; 

	        //System.out.println("I'm in compare line!"); 
	        for(int i=0; i<5; i++){ 
	            if(home.get(i) - away.get(i) >= 3){
	                homeScore ++; 
	            }
	            if(away.get(i) - home.get(i) >= 3){ 
	                homeScore --; 
	            }
	        }
	        return homeScore; 
    	}

	    

    	private List<Integer> bestPermutation(List<Integer> home, List<Integer> away){ 
	        bestLine.clear(); 
	        score = -100; 
	        counter = 0; 

	        permute(home, 0, away); 

	        return bestLine; 

    	}
}
