package matchup.g2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.lang.Math;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

// To get game history.
import matchup.sim.utils.*;

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
    
    private List<Integer> strategy = Arrays.asList(-3,2,10,9,8,7,6,-4,1,5,0,-5,-1,4,-6,-7,-8,-9,-10,-2,3);
    private ArrayList<ArrayList<Integer>> prefLists;
    private ArrayList<ArrayList<Integer>> opponentPrefLists;
    private Line lineup;
    private Line opponentLineup;
    
    
    
    
    private int counter; 
    
    private Map<Integer, Double> historySkillCount;
    private Map<Integer, Long> historySkillPercents;
    private Map<String, List<Double>> historySkillStats;
    private Map<String, Double> aveSkillHistory;
    private Map<String, Map<Integer, List<Double>>> historyLineStats;
    private Map<String, Map<Integer, Double>> aveLineHistory;
    private Map<Integer, List<Integer>> popularSkills;
    
    
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
	
	double sum = 0.0;
	double mean = 0.0;
	for (int i : skills) {
	    sum += i;
	}
	mean = sum/skills.size();
	stats.put("mean", mean);
	
	skills.sort(null);
	
	// Min and max
	double min, max;
	min = skills.get(0);
	max = skills.get(skills.size()-1);
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
	stdev = Math.sqrt(sqr_sum/(skills.size()-1));
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

    public List<Integer> getSkills() {
	
	// ###############################################
	// ### Gather information about previous games ###
	// ###############################################
	
	// Get history of games
	//-> this is the first function called once a round has started so we can collect history info in this function
	//-> store the information in class members for other functions to use
	List<Game> games = History.getHistory();
	double numGamePairs = games.size();
	System.out.println(games.size());
	
	PlayerData opponent;
	List<Integer> oppSkills;
	Map<String, Double> oppSkillStats;
	Map<Integer, Integer> oppSkillCount;
	
	List<String> statsLong = new ArrayList<String>(Arrays.asList("stdev", "mean", "min", "max", "range"));
	List<String> statsShort = new ArrayList<String>(Arrays.asList("stdev", "min", "max"));
	
	// historySkillCount keeps track of the number of times each skill have shown up across all previous games
	historySkillCount = new HashMap<Integer, Double>();
	for (int i=1; i<12; i++) {
	    historySkillCount.put(i, 0.0);
	}
	
	// historySkillPercents keeps track of the frequency of each skill across all previous games
	historySkillPercents = new HashMap<Integer, Long>();
	
	// historySkillStats keeps track of the statistics for the overall skill distributions in each previous game
	historySkillStats = new HashMap<String, List<Double>>();
	for (String s : statsShort) {
	    historySkillStats.put(s, new ArrayList<Double>());
	}
	
	// aveSkillHistory keeps track of the average statistics for the overall skill distribution across all previous games
	aveSkillHistory = new HashMap<String, Double>();
	for (String s : statsShort) {
	    aveSkillHistory.put(s, 0.0);
	}
	
	// historyLineStats keeps track of the statistics for each of the opponent's lines
	historyLineStats = new HashMap<String, Map<Integer, List<Double>>>();
	//the lines are ordered according to the order in which they are played in the game
	for (String s : statsLong) {
	    historyLineStats.put(s, new HashMap<Integer, List<Double>>());
	    for (int i=0; i<3; i++) {
		historyLineStats.get(s).put(i, new ArrayList<Double>());
	    }	
	}
	
	// aveLineHistory keeps track of the average statistics for each of the opponent's lines across all previous games
	aveLineHistory = new HashMap<String, Map<Integer, Double>>();
	for (String s : statsLong) {
	        	aveLineHistory.put(s, new HashMap<Integer, Double>());
	}
	
	// popularSkills contains a single list of skills in order of decreasing frequency of use in the opponent's skill distribution
	popularSkills = new LinkedHashMap<Integer, List<Integer>>();
	List<Double> sortedVals = new ArrayList<Double>();
	
	
	boolean notNull = false;
	for (Game g : games) {
	    
	    notNull = false;
	    // Find out which player is the opponent
	    if (g.playerA.name.equals("g2")) {
			opponent = g.playerB;
			oppSkills = g.playerB.skills; 
	    } else {
			opponent = g.playerA;
			oppSkills = g.playerA.skills;
	    }
	    
	    if (!oppSkills.isEmpty()) {
		notNull = true;
		
		oppSkillStats = getSkillStats(oppSkills);
		oppSkillCount = getSkillCount(oppSkills);
		
		// Update the total skill count for the skills in the overall distribution
		for (int s : oppSkillCount.keySet()) {
		    double count_current = historySkillCount.get(s);
		    count_current += oppSkillCount.get(s);
		    historySkillCount.replace(s, count_current);
		}
		//System.out.println("historySkillCount: " + historySkillCount); //
		
		// Add the statistics of the overall skill distribution for this round to historySkillStats
		for (String stat : historySkillStats.keySet()) {
		    historySkillStats.get(stat).add(oppSkillStats.get(stat));
		}
		//System.out.println("historySkillStats: " + historySkillStats); //
		
		// For each line, add the statistics of the line skill distribution for this round to historyLineStats
		for (int i=0; i<3; i++) {
		    List<Integer> line = opponent.rounds.get(i);
		    Map<String, Double> lineStats= getSkillStats(line);
		    for (String stat : historyLineStats.keySet()) {
			historyLineStats.get(stat).get(i).add(lineStats.get(stat));
		    }
		}
		//System.out.println("historyLineStats: " + historyLineStats); //
	    }
	    
	}
	
	Long maxSkillUsed = 0L;
	int maxSkill = 0;
	
	if (notNull) {
	    
	    //System.out.println("historySkillCount: " + historySkillCount); //
	    
	    // Get percentages for the skill counts in the overall skill distribution
	    for (int s : historySkillCount.keySet()) {
		//System.out.println(historySkillCount.get(s));
		double percent = (historySkillCount.get(s)/(numGamePairs*15.0))*100.0;
		historySkillPercents.put(s, Math.round(percent));
	    }
	    //System.out.println("historySkillPercents: " + historySkillPercents); //
	    
	    // Get averages for the overall skill distribution statistics		
	    for (String stat : historySkillStats.keySet()) {
		double total = 0.0;
		for (int val=0; val<historySkillStats.get(stat).size(); val++) {
		    total += val;
		}
		aveSkillHistory.replace(stat, total/historySkillStats.get(stat).size());
	    }
	    //System.out.println("aveSkillHistory: " + aveSkillHistory); //
	    
	    // Get averages for the line statistics
	    for (String stat : historyLineStats.keySet()) {
		for (int i=0; i<3; i++) {
		    double total = 0.0;
		    for (int val=0; val<historyLineStats.get(stat).get(i).size(); val++) {
			total += val;
		    }
		    aveLineHistory.get(stat).put(i, total/historyLineStats.get(stat).get(i).size());
		}
	    }
	    //System.out.println("aveLineHistory: " + aveLineHistory); //
	    
	    // Create a list of skills in order of decreasing frequency of use in the opponents skill distribution
	    // Each pair consists of a count and a list of all skills with that count (to catch the case in which there more than one skill is used the most)
	    sortedVals = new ArrayList(historySkillCount.values());
	    sortedVals.sort(null);
	    Collections.reverse(sortedVals);
	    System.out.println(sortedVals);

	    List<Integer> usedVals = new ArrayList<Integer>();
	    for (Double doubleVal : sortedVals) {
	    int val = doubleVal.intValue();
		if (!usedVals.contains(val)) {
		    List<Integer> sList = new ArrayList<Integer>();
		    for (int s : historySkillCount.keySet()) {
			if (historySkillCount.get(s).intValue() == val)
			    sList.add(s);
		    }
		    popularSkills.put(val, sList);
		    usedVals.add(val);
		}
	    }
	    System.out.println("popularSkills: " + popularSkills); //
		
	}			
	System.out.println("!!!");

	
		// ##########################################################
		// ### End collection of information about previous games ###
		// ##########################################################
	
		if (games.size() > 1) {
			System.out.println("not null");

			int strategy = 2;

		//*************
		// STRATEGY # 1
		//*************

		if (strategy == 0){
			skills = new ArrayList<Integer>();

			maxSkill = 0;
			int minSkill = 12;

			double maxCount = 0;
			double minCount = 0;
			// iterate over most popular skills

			//e.g.
			//popularSkills: {6=[6], 4=[3, 4, 8, 9], 2=[2, 5, 7, 10], 0=[1, 11]}
			System.out.println("Second game");
			int maxIndex = sortedVals.get(0).intValue();
			int secmaxIndex = sortedVals.get(1).intValue();
			List<Integer> mostUsed = new ArrayList<Integer>(popularSkills.get(maxIndex));
			System.out.println(mostUsed);
			List<Integer> secMostUsed = new ArrayList<Integer>(popularSkills.get(secmaxIndex));
			System.out.println(secMostUsed);



		    for (int i =0; i < (maxIndex / games.size()/2) *15;i++){ 
	
		    	// if (mostUsed.get(i) > maxSkill){
		    	// 	maxSkill = mostUsed.get(i);
		    	// }
		    	// if ((mostUsed.get(i) < minSkill) && ( mostUsed.get(i) < 8)){
		    	// 	minSkill = mostUsed.get(i);
		    	// }else
		    	// {

		    	// 	minSkill = 4;
		    	// }
		    	skills.add(mostUsed.get(0));


		    }
		    maxCount = (int)((double)historySkillCount.get(maxSkill));
		    minCount = (int)((double)historySkillCount.get(minSkill));


		    // for (int j = 0 ; j < (maxCount*15)/games.size()/2 && (j < 15);j++ ){

		    // 	skills.add(maxSkill-2);
		    // }
		    // for (int j = 0 ; j < (minCount*15)/games.size()/2 && (j < 15);j++ ){

		    // 	skills.add(minSkill+3);
		    // }

		    

		    int sumSkills = 0 ;
		    for (int s : skills){
		    	sumSkills += s;
		    }


		    int numsLeft = 15 - skills.size();
		    int skillsLeft = 90 - sumSkills;
		    int nextMean = 7;

		    for ( int k = 0 ; k < numsLeft-1; k++){


		    	nextMean = numsLeft / skillsLeft ; 
		    	int newSkill = rand.nextInt(3)*(-1)^k + nextMean;
		    	skills.add(newSkill);

		    	numsLeft--;
		    	skillsLeft -= newSkill;

		    }
		    skills.add(skillsLeft);

		    System.out.println(skills);


		}
		//*************
		// STRATEGY # 2
		//*************
		//Description:
		// random set of skills from 5 probabalistically good lines
		else if (strategy == 1){
			int pickRandLine;
			pickRandLine = rand.nextInt(5);

			skills = new ArrayList<Integer>();

			// GOOD RANDOM LINE = [ 1 1 1 1 1 4 9 9 9 9 9 9 9 9 9]
			if (pickRandLine == 0){
				skills = new ArrayList<Integer>(Arrays.asList(1,1,1,1,1,4,9,9,9,9,9,9,9,9,9));
			}

			// GOOD RANDOM LINE = [ 4 4 4 4 4 7 7 7 7 7 7 7 7 7 7]
			else if (pickRandLine == 1){
				skills = new ArrayList<Integer>(Arrays.asList(4,4,4,4,4,7,7,7,7,7,7,7,7,7,7));
			}
			else if (pickRandLine == 2){
				skills = new ArrayList<Integer>(Arrays.asList(1,1,1,1,1,8,8,8,8,8,9,9,9,9,9));
			}
			else if (pickRandLine == 3){
				skills = new ArrayList<Integer>(Arrays.asList(4,4,4,4,4,6,6,6,6,6,8,8,8,8,8));
			}
			else if (pickRandLine == 4){
				skills = new ArrayList<Integer>(Arrays.asList(2,2,2,2,2,6,6,6,6,6,10,10,10,10,10));
			}

		}


		//*************
		// STRATEGY # 3
		//*************
		//Description:
		// 2 below theyre highest 9 numbers 3 above lowest 6
		else if (strategy == 2){

			
				Game lastGame =  games.get(games.size()-1);
				List<Integer> oppLastSkills;
				skills = new ArrayList<Integer>();

				if (lastGame.playerA.name.equals("g2")){
					oppLastSkills = new ArrayList<Integer>(lastGame.playerB.skills);
					
				}
				else{
					oppLastSkills = new ArrayList<Integer>(lastGame.playerA.skills);
				}
				for (int i = 0 ; i < 15; i++){
					if (i<6){
						skills.add(oppLastSkills.get(i) + 3);
					}
					else
						skills.add(oppLastSkills.get(i) - 2);
				}
				// System.out.println(oppLastSkills);

			
			
			

		}
	} else {
		skills = new ArrayList<Integer>(Arrays.asList(1,1,1,1,1,4,9,9,9,9,9,9,9,9,9));

	}
		return skills;
    }
    
    public List<List<Integer>> getDistribution(List<Integer> opponentSkills, boolean isHome) {

		distribution = new ArrayList<List<Integer>>();
		home = isHome;
		opponentLineup = new Line(opponentSkills, false);
		lineup = new Line(skills, true);

		skills.sort(null);

		opponentSkills.sort(null);
		// get stats on our skills
		Map<String, Double> ourStats = getSkillStats(skills);

		// get our skill count
		Map<Integer, Integer> selfSkillCount = getSkillCount(skills);

		// get stats on opponent's skills
		Map<String, Double> oppStats = getSkillStats(opponentSkills);

		// get opponent's skill count
		Map<Integer, Integer> oppSkillCount = getSkillCount(opponentSkills);

		// get our skill mapping: for each of our skills find out which of the opponent's skills it will beat, tie to, lose against
		Map<String, Map<Integer, List<Integer>>> selfSkillMapping = getSkillMapping(skills, opponentSkills);

		// get opponent's sill mapping: for each of opponent's skills find out which of our skills it will beat, tie to, lose against
		Map<String, Map<Integer, List<Integer>>> oppSkillMapping = getSkillMapping(opponentSkills, skills);


		// #############
		// ### HOME ####
		// #############

		if (isHome) {
			// -- Arrange rows to be optimal for HOME play --

			int strategy;

			strategy = rand.nextInt(3);
			System.out.println(strategy);

			if (strategy == 0) {
				// pick strategy 0

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
				

				// -- Distribute win skills into 3 lines --
				for (int c=0; c<3; c++)
					distribution.add(new ArrayList<Integer>());

				for (int win_count : wins) {
					for (int win_skill : win_skills.keySet()) {
						if (win_skills.get(win_skill).get(1) == win_count) {
							int i=0;
							int added=0;
							while (added != win_skills.get(win_skill).get(0)) {
								if (distribution.get(i%3).size() < 5) {
									distribution.get(i%3).add(win_skill);
									i++;
									added++;
								} else {
									i++;
								}
							}
						}
					}
				}


				// get the skills that tie against at least 1 thing (but then this can be leveraged with counts)
				// first we need to remove any of the win skills we've already distributed
				for (int win_skill : win_skills.keySet())
					selfSkillMapping.get("ties_against").remove(win_skill);
				
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

					// -- Distribute tie skills into 3 lines --
					for (int tie_count : ties) {
						for (int tie_skill : tie_skills.keySet()) {
							if (tie_skills.get(tie_skill).get(1) == tie_count) {
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
				}

				// get the skills that lose against at least 1 thing (but then this can be leveraged with counts)
				// first we need to remove any of the win and tie skills we've already distributed
				for (int win_skill : win_skills.keySet())
					selfSkillMapping.get("loses_against").remove(win_skill);
				for (int tie_skill : tie_skills.keySet())
					selfSkillMapping.get("loses_against").remove(tie_skill);
				
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

					// -- Distribute tie skills into 3 lines --
					for (int lose_count : losses) {
						for (int lose_skill : lose_skills.keySet()) {
							if (lose_skills.get(lose_skill).get(1) == lose_count) {
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
				}


			} else if (strategy == 1) {
				// pick strategy 1

				List<Integer> row1, row2, row3;

				row1 = new ArrayList<Integer>(Arrays.asList(skills.get(14), skills.get(12), skills.get(10), skills.get(0), skills.get(7)));
				row2 = new ArrayList<Integer>(Arrays.asList(skills.get(13), skills.get(11), skills.get(9), skills.get(1), skills.get(8)));
				row3 = new ArrayList<Integer>(Arrays.asList(skills.get(2), skills.get(3), skills.get(4), skills.get(5), skills.get(6)));

				distribution.add(row1);
				distribution.add(row2);
				distribution.add(row3);



			} else if (strategy == 2) {
				//pick strategy 2

				List<Integer> row1, row2, row3;

				row1 = new ArrayList<Integer>(Arrays.asList(skills.get(14), skills.get(13), skills.get(12), skills.get(3), skills.get(11)));
				row2 = new ArrayList<Integer>(Arrays.asList(skills.get(0), skills.get(1), skills.get(2), skills.get(4), skills.get(10)));
				row3 = new ArrayList<Integer>(Arrays.asList(skills.get(5), skills.get(6), skills.get(7), skills.get(8), skills.get(9)));

				distribution.add(row1);
				distribution.add(row2);
				distribution.add(row3);

			}


		// #############
		// ### AWAY ####
		// #############
		} else {
			// arrange rows to be optimal for AWAY play

			int strategy;

			strategy = rand.nextInt(2);
			System.out.println(strategy);

			if (strategy == 0) {
				// pick strategy 0

				List<Integer> row1, row2, row3;

				row1 = new ArrayList<Integer>(Arrays.asList(skills.get(14), skills.get(13), skills.get(12), skills.get(3), skills.get(11)));
				row2 = new ArrayList<Integer>(Arrays.asList(skills.get(0), skills.get(1), skills.get(2), skills.get(4), skills.get(10)));
				row3 = new ArrayList<Integer>(Arrays.asList(skills.get(5), skills.get(6), skills.get(7), skills.get(8), skills.get(9)));

				distribution.add(row1);
				distribution.add(row2);
				distribution.add(row3);

			} else if (strategy == 1) {
				// pick strategy 1

				List<Integer> row1, row2, row3;

				row1 = new ArrayList<Integer>(Arrays.asList(skills.get(14), skills.get(12), skills.get(10), skills.get(0), skills.get(7)));
				row2 = new ArrayList<Integer>(Arrays.asList(skills.get(13), skills.get(11), skills.get(9), skills.get(1), skills.get(8)));
				row3 = new ArrayList<Integer>(Arrays.asList(skills.get(2), skills.get(3), skills.get(4), skills.get(5), skills.get(6)));

				distribution.add(row1);
				distribution.add(row2);
				distribution.add(row3);

			}
			
		}

		return distribution;
	}

	public void clear() {
		availableRows.clear();
		for (int i = 0; i < 3; ++i)
			availableRows.add(i);
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
		//	System.out.println("selected line: " + n);
		availableRows.remove(n);
		
		List<Integer> round = distribution.get(n);
		
		if (home) {
		    round = bestPermutation(round, opponentRound);
		}
		
		lineup.removePlayers(round);
		return round;
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
		//System.out.println(chooseFirst + "wins with first");
		return availableRows.get(0);
	    } else {
		//System.out.println(chooseSecond + "wins with second");
		return availableRows.get(1);
		}
	} else { //selecting first home line
	    
	    List<Integer> opponentRemaining = opponentLineup.valueList();
	    List<Integer> lineScore = Arrays.asList(0, 0, 0);
	    
	    //System.out.println("Opponents remaining: " + opponentRemaining);
	    
	    for (Integer i : availableRows) { // 0-2
		Line temp = new Line(skills, true);
		temp.removePlayers(distribution.get(i));
		
		opponentLineup.clear();
		GaleShapely(temp, opponentLineup);
		
		lineScore.set(i, temp.score()-5*totalLineWins(distribution.get(i), opponentRound));
	    }
	    //System.out.println("linescores: " + lineScore);
	    
	    int minScore = 1000;
	    for(int i: lineScore){
		if (i < minScore){
		    minScore = i;
		}
	    }
	    return lineScore.indexOf(minScore);
	}
    }
    
    
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
