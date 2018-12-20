package matchup.g3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import matchup.sim.utils.Game;
import matchup.sim.utils.History;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Player implements matchup.sim.Player {
	private List<Integer> skills;
	private List<List<Integer>> distribution;
	private List<Integer> availableRows;
	
	private List<Integer> counterOffsets;
	// private Map<String, Integer> recur;
	private List<List<Integer>> pool;

	private Random rand;
	
	private boolean ishome = true; 
	private int iterations;
	
	public Player() {
		rand = new Random();
		skills = new ArrayList<Integer>();
		distribution = new ArrayList<List<Integer>>();
		availableRows = new ArrayList<Integer>();
		ishome = false;
		counterOffsets = new ArrayList<Integer>();
		// recur = new HashMap<String, Integer>();
		iterations = 0;

		for (int i=0; i<3; ++i) availableRows.add(i);
	}
	
    public void init(String opponent) {
    	pool = new ArrayList<>();
		ArrayList<Integer> fixed = new ArrayList<>();
		for(int i=0; i<3 ;i++){
			fixed.add(10);
			fixed.add(4);
			fixed.add(7);
			fixed.add(3);
			fixed.add(6);
		}

		ArrayList<Integer> fixed1 = new ArrayList<>();
		for(int i=0; i<5 ;i++){
			fixed1.add(2);
		}
		for(int i=0; i<5 ;i++){
			fixed1.add(6);
		}
		for(int i=0; i<5 ;i++){
			fixed1.add(10);
		}

		ArrayList<Integer> fixed2 = new ArrayList<>();
		for(int i=0 ;i< 15; i++){
			fixed2.add(6);
		}

		ArrayList<Integer> fixed3 = new ArrayList<>();
		for(int i=0; i<5 ;i++){
			fixed3.add(2);
		}
		for(int i=0; i<5 ;i++){
			fixed3.add(7);
		}
		for(int i=0; i<5 ;i++){
			fixed3.add(9);
		}

		ArrayList<Integer> fixed4 = new ArrayList<>();
		for(int i=0; i<5 ;i++){
			fixed4.add(1);
		}
		for(int i=0; i<5 ;i++){
			fixed4.add(8);
		}
		for(int i=0; i<5 ;i++){
			fixed4.add(9);
		}

		pool.add(fixed);
		pool.add(fixed1);
		pool.add(fixed2);
		pool.add(fixed3);
		pool.add(fixed4);
    }

	public List<Integer> getSkills() {
//		for (int i=0; i<7; ++i) {
//			int x = rand.nextInt(11) + 1;
//			skills.add(x);
//			skills.add(12 - x);
//		}
//
//		skills.add(6);
//		Collections.shuffle(skills);
//		
//		return skills;
		// analyzeLastIteration();
		iterations += 1;
    	skills.clear();
    	counterOffsets.clear();
		List<Game> data = History.getHistory();
//		//System.out.println("before map Building");
		// buildRecurMap(data);
		
		if (isConsecutiveSameSkills(data)) {
			//System.out.println("consecutive");
			skills = perfectCounter(getOppSkills(data.get(data.size() - 1)));
			return skills;
		}
		
		analyzeTwoInterations(data);
		if (counterOffsets.size() > 0) {
			List<Integer> ourLastSkills = getOurSkills(data.get(data.size() - 1));
//			for (int i = 0; i < counterOffsets.size(); i++) {
//				//System.out.println("our skill:" + ourLastSkills.get(i) + ", offset: " + counterOffsets.get(i));
//				skills.add(ourLastSkills.get(i) + counterOffsets.get(i));
//			}
			skills = perfectCounter(ourLastSkills);
			skills = perfectCounter(skills);
			// skills = perfectCounter(skills);
			
			return skills;
		}
		
		analyzeLastIteration(data);
		if (counterOffsets.size() > 0) {
			// getNewSkills + counter * 2
			Random random = new Random();
			skills = pool.get(random.nextInt(4) + 0);
			for (int i = 0; i < 2; i++) {
				skills = perfectCounter(skills);
			}
			return skills;
		}
		
//		if (iterations % 20 == 0) {
//			// purgeMap();
//			//System.out.println("purgeMap");
//		}
//		
//		if (recur.size() > 0 && iterations > 20) {
//			List<String> strings = new ArrayList<String>(recur.keySet());
//			//System.out.println("all recur keys: " + strings);
//			Random r = new Random();
//			List<Integer> opp = stringToList(strings.get(r.nextInt(strings.size() - 1)));
//			//System.out.println("counter recur");
//			return perfectCounter(opp);
//		}
		
		// return random strong numbers
		Random random = new Random();
		skills = pool.get(random.nextInt(5));
		return skills;
	}
	
	List<Integer> getOppSkills(Game game) {
		if (game.playerA.name.equals("g3")) {
			return new ArrayList<Integer>(game.playerB.skills);
		}
		else {
			return new ArrayList<Integer>(game.playerA.skills);
		}
	}
	
	List<Integer> getOurSkills(Game game) {
		if (game.playerA.name.equals("g3")) {
			return new ArrayList<Integer>(game.playerA.skills);
		}
		else {
			return new ArrayList<Integer>(game.playerB.skills);
		}
	}
	
//	private void purgeMap() {
//		for (Map.Entry<String, Integer> entry : recur.entrySet())  
//            if (entry.getValue() < 3) {
//            	recur.remove(entry.getKey());
//            }
//	}
	
	private String listToString(List<Integer> l) {
		Collections.sort(l);
		StringBuilder builder  = new StringBuilder();
		for (Integer i: l) {
			builder.append(i);
		}
		return builder.toString();
	}
	
	private List<Integer> stringToList(String s) {
		Scanner scanner = new Scanner(s);
		List<Integer> list = new ArrayList<Integer>();
		while (scanner.hasNextInt()) {
		    list.add(scanner.nextInt());
		}
		return list;
	}
	
	private boolean isConsecutiveSameSkills(List<Game> data) {
		if (data.size() < 6) {
			return false;
		}
		
		Game last = data.get(data.size() - 1);
		List<Integer> opp;
		if (last.playerA.name.equals("g3")) {
			opp = new ArrayList<Integer>(last.playerB.skills);
		}
		else {
			opp = new ArrayList<Integer>(last.playerA.skills);
		}
		
		for (int i = 1; i < 3; i++) {
			Game game = data.get(data.size() - i * 2 - 2);
			if (game.playerA.name.equals("g3")) {
				if (!opp.equals(game.playerB.skills)) {
					return false;
				}
			}
			else {
				if (!opp.equals(game.playerA.skills)) {
					return false;
				}
			}
		}
		
		return true;
	}
//	
//	private void buildRecurMap(List<Game> data) {
//		if (data.size() > 0) {
//			List<Integer> oppSkills = getOppSkills(data.get(data.size() - 1));
//			//System.out.println("oppSkills got");
//			String s = listToString(oppSkills);
//			//System.out.println("converted to string");
//			if (recur.containsKey(s)) {
//				recur.put(s, recur.get(s) + 1);
//			}
//			else {
//				recur.put(s, 1);
//			}
//		}
//		//System.out.println("map built");
//	}

    public List<List<Integer>> getDistribution(List<Integer> opponentSkills, boolean isHome) {
    	Collections.sort(skills);
    	List<Integer> index = new ArrayList<Integer>();
    	for (int i=0; i<15; ++i) index.add(i);

    	distribution = new ArrayList<List<Integer>>();

		if (isHome) {
			ishome = true;
			distribution =  varyTeamSkills();
			return distribution;
		}
		
		ishome = false;
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
    

    public List<Integer> playRound(List<Integer> opponentRound) {
    	
    	if (opponentRound == null || !ishome) {
    		ishome = false;
    		int nextRow = availableRows.remove(0);
    		return new ArrayList<Integer>(distribution.get(nextRow));
    	}
    	
    	ishome = true;
    	int[][] teams = new int[availableRows.size()][5];
    	int[] wins = new int[availableRows.size()];
    	double[] wastes = new double[availableRows.size()];
    	double[] efficiencies = new double[availableRows.size()];
    	for (int i = 0; i < availableRows.size(); i++) {
    		List<Integer> team = distribution.get(availableRows.get(i));
    		boolean[] marked = new boolean[5];
    		double totalSkillsLost = 0;
    		int totalNetWin = 0;
    		for (int j = 0; j < team.size(); j++) {
    			Map<Stat, PriorityQueue<wasteCounter>> map = new EnumMap<Stat, PriorityQueue<wasteCounter>>(Stat.class);
    			for (int e = 0; e < opponentRound.size(); e++) {
    				if (marked[e]) {
    					continue;
    				}
    				
    				int diff = team.get(j) - opponentRound.get(e);
    				if (diff >= 3) {
    					// win, (diff - 3) is the amount of waste of skills
    					if (!map.containsKey(Stat.WIN)) {
    						PriorityQueue<wasteCounter> order = new PriorityQueue<wasteCounter>(5, new WasteComparator());
    						order.add(new wasteCounter(e, diff - 3));
    						map.put(Stat.WIN, order);
    					}
    					else {
    						map.get(Stat.WIN).add(new wasteCounter(e, diff - 3));
    					}
    				}
    				else if (diff >= -2 && diff <= 2) {
    					// tie, (diff + 2) is the amount of waste of skills
    					if (!map.containsKey(Stat.TIE)) {
    						PriorityQueue<wasteCounter> order = new PriorityQueue<wasteCounter>(5, new WasteComparator());
    						order.add(new wasteCounter(e, diff + 2));
    						map.put(Stat.TIE, order);
    					}
    					else {
    						map.get(Stat.TIE).add(new wasteCounter(e, diff + 2));
    					}
    				}
    				else {
    					// lose, (diff + 3) is the amount of waste of skills, in the case of losing, it's usually a gain of skills
    					if (!map.containsKey(Stat.LOSE)) {
    						PriorityQueue<wasteCounter> order = new PriorityQueue<wasteCounter>(5, new WasteComparator());
    						order.add(new wasteCounter(e, diff + 3));
    						map.put(Stat.LOSE, order);
    					}
    					else {
    						map.get(Stat.LOSE).add(new wasteCounter(e, diff + 3));
    					}
    				}
    			}
    			
    			Stat status;
    			if (map.containsKey(Stat.WIN)) {
    				totalNetWin += 1;
    				status = Stat.WIN;
    			}
    			else if (map.containsKey(Stat.TIE)) {
    				status = Stat.TIE;
    			}
    			else {
    				totalNetWin -= 1;
    				status = Stat.LOSE;
    			}
    			
    			wasteCounter wc= map.get(status).peek();
    			totalSkillsLost += wc.waste;
    			int index = wc.index;
    			marked[index] = true;
    			teams[i][index] = team.get(j);
    		}
    		wins[i] = totalNetWin;
    		wastes[i] = totalSkillsLost;
    		if (wins[i] < 0 && wastes[i] > 0) {
    			wins[i] = - wins[i];
    			wastes[i] *= 4;
    		}
    		
    		if (wins[i] > 0 && wastes[i] < 0) {
    			wastes[i] = - wastes[i] / 4;
    		}
    		efficiencies[i] = (wins[i] + 0.001) / (wastes[i] + 0.0001);
    	}
    	
    	// find the positive wins and negative wastes
    	double score = - Double.MAX_VALUE;
    	int index = -1;
    	List<Integer> result = new ArrayList<Integer>();
    	for (int i = 0; i < efficiencies.length; i++) {
    		if (efficiencies[i] > score) {
    			index = i;
    			score = efficiencies[i];
    		}
    	}

    	for (int i = 0; i < teams[index].length; i++) {
    		result.add(teams[index][i]);
    	}
    	availableRows.remove(index);
    	return result;
    }
    
    private void backtrace(List<List<Integer>> result, List<Integer> tempList, List<Integer> round, int start) {
    	if(start==5) {
    		result.add(new ArrayList<>(tempList));
    		return;
    	} 
    	Integer temp=0;
    	for(int i=0;i<round.size();i++) {
    		temp = round.get(0);
    		tempList.add(temp);
    	    round.remove(round.indexOf(temp));
    	  	backtrace(result, tempList, round , start+1); 
    		tempList.remove(tempList.size()-1);
    		round.add(temp);
    	}
  
    }
    public void clear() {
    	availableRows.clear();
    	for (int i=0; i<3; ++i) availableRows.add(i);
    }
    
    private List<List<Integer>> varyTeamSkills() {
    	List<List<Integer>> varied = new ArrayList<List<Integer>>();
    	
    	for (int i = 0; i < 3; i++) {
    		List<Integer> row = new ArrayList<Integer>();
    		for (int j = 0; j < 5; j++) {
    			row.add(skills.get(i + j * 3));
    		}
    		varied.add(row);
    	}
    	
    	return varied;
    }
    
    private void analyzeLastIteration(List<Game> games) {
    	if (games.size() <= 0) {
    		return;
    	}
    	
    	Game game = games.get(games.size() - 1);
    	
    	List<Integer> ourSkills = getOurSkills(game);
    	List<Integer> oppSkills = getOppSkills(game);
    	
    	counterOffsets = analyzeSkills(ourSkills, oppSkills);
    }
    
    private void analyzeTwoInterations(List<Game> games) {
    	if (games.size() < 4) {
    		return;
    	}
    	
    	Game first = games.get(games.size() - 3);
    	Game second = games.get(games.size() - 1);
    	
    	List<Integer> ourSkillsFirst = getOurSkills(first);
    	List<Integer> oppSkillsSecond = getOppSkills(second);
    	
    	// home and away skills are the same for one iteration, so redundancy is expected. Not the same case for a single round though
    	counterOffsets = analyzeSkills(ourSkillsFirst, oppSkillsSecond);
    }
    
    private List<Integer> analyzeSkills(List<Integer> id, List<Integer> opp) {
    	List<Integer> idCopy = new ArrayList<Integer>(id);
    	List<Integer> oppCopy = new ArrayList<Integer>(opp);
    	
    	Collections.sort(idCopy);
    	Collections.sort(oppCopy);
    	
    	int win = 0;
    	int tie = 0;
    	// these two are potentially useless
    	List<Integer> winOffset = new ArrayList<Integer>();
    	List<Integer> tieOffset = new ArrayList<Integer>();
    	for (int i = 0; i < idCopy.size(); i++) {
    		if (oppCopy.get(i) - idCopy.get(i) >= 3) {
    			win += 1;
    			winOffset.add(oppCopy.get(i) - idCopy.get(i));
    		}
    		else if (oppCopy.get(i) - idCopy.get(i) >= -2 && oppCopy.get(i) - idCopy.get(i) <= 2) {
    			tie += 1;
    			tieOffset.add(idCopy.get(i) - oppCopy.get(i));
    		}
    	}
    	
    	List<Integer> counterOffset = new ArrayList<Integer>();
    	if (win > 3 && tie > 5) {
    		counterOffset.clear();
    		for (int i = 0; i < oppCopy.size(); i++) {
    			counterOffset.add(oppCopy.get(i) - idCopy.get(i));
    		}
    	}
    	
    	return counterOffset;
    }

    
    private List<Integer> perfectCounter(List<Integer> s) {
    	List<Integer> copy = new ArrayList<Integer>(s);
    	Collections.sort(copy);
    	List<Integer> newSkills = new ArrayList<Integer>();
    	for (int i = 0; i < 6; i++) {
    		int oppSkills = copy.get(i);
    		newSkills.add(oppSkills + 3);
    	}

    	for (int i = 6; i < 15; i++) {
    		int oppSkills = copy.get(i);
    		newSkills.add(oppSkills - 2);
    	}
    	return newSkills;
    }
    
    private List<Integer> altCounter(List<Integer> s) {
    	List<Integer> copy = new ArrayList<Integer>(s);
    	Collections.sort(copy);
    	List<Integer> newSkills = new ArrayList<Integer>();
    	for (int i = 0; i < 5; i++) {
    		int oppSkills = copy.get(i);
    		newSkills.add(oppSkills + 4);
    	}

    	for (int i = 5; i < 15; i++) {
    		int oppSkills = copy.get(i);
    		newSkills.add(oppSkills - 2);
    	}
    	return newSkills;
    }
    
	public List<Integer> getRandomSkills() {
		for (int i=0; i<7; ++i) {
			int x = rand.nextInt(11) + 1;
			skills.add(x);
			skills.add(12 - x);
		}

		skills.add(6);
		Collections.shuffle(skills);

		return skills;
	}
    
    private enum Stat {
    	WIN, TIE, LOSE
    }
    
    private class wasteCounter {
    	private int index;
    	private double waste;
    	
    	public wasteCounter(int index, double waste) {
    		this.index = index;
    		this.waste = waste;
    	}
    }
    
    class WasteComparator implements Comparator<wasteCounter> {

		@Override
		public int compare(wasteCounter o1, wasteCounter o2) {
			// TODO Auto-generated method stub
			if (o1.waste < o2.waste) {
				return -1;
			}
			else if (o1.waste > o2.waste) {
				return 1;
			}
			return 0;
		}
    	
    }
    
    public static void main(String[] args) {
    	for (int j = 0; j < 100; j++) {
        	Player p = new Player();
        	List<Integer> skills = p.getRandomSkills();
        	List<Integer> counter = p.altCounter(skills);
        	int total = 0;
        	for (int i = 0; i < counter.size(); i++) {
        		total += counter.get(i);
        	}
        	if (total != 90) {
        		//System.out.println("total not 90   *****" + total);
        	}
        	//System.out.println("skills: " + skills);
        	//System.out.println("counter: " + counter);
    	}
    }
}
