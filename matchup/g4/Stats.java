package matchup.g4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

// To get game history.
import matchup.sim.utils.*;

class Stats {

	private List<Game> games;
	private Skills predOppSkills;
    private List<List<Integer>> opskills;
	private boolean isPlayerA;
    private Skills curSkills;
	private int lossStreak;
    private boolean doCounter;
    private int counterTimer;

	// private boolean detectedTwoStep;
	// private boolean immediateCounter;
	// private boolean twoStepCounter;

    public int totalTies;
    public int totalWins;
    public int totalLoss;
    public int currTies;
    public int currWins;
    public int currLoss;

	public Stats() {
		games = History.getHistory();
		isPlayerA = games.get(0).playerA.name.equals("g4");
        // detectedTwoStep = false;
        // immediateCounter = false;
        // twoStepCounter = false;      
        doCounter = false; 
        counterTimer = 0;
		lossStreak = 0;
        totalTies = 0;
        totalWins = 0;
        totalLoss = 0;
        currTies = 0;
        currWins = 0;
        currLoss = 0;
        Integer s[] = {9,9,9,9,9,9,9,9,9,4,1,1,1,1,1};
        curSkills = new Skills(Arrays.asList(s));
	}

    public Skills getSkills() {
        return curSkills;
    }

	public void update() {
		games = History.getHistory();
        checkLoss();

        //System.out.println("Hi");

        if (doCounter) {
            if (counterTimer == 0) {
                curSkills = new Skills(counter(counter(curSkills)));
            } else {
                counterTimer--;
            }
        }

        //System.out.println("Hi");

        if (lossStreak >= 3) {
            doCounter = false;
            doLoss();
            checkCounterStrategy();

            if(doCounter) {
                curSkills = new Skills(counter(counter(curSkills)));
            } else if (isConverging()) {
                curSkills = new Skills(counter(predOppSkills));
            } else {
                Integer s[] = {9,9,9,9,9,9,9,9,9,4,1,1,1,1,1};
                curSkills = new Skills(Arrays.asList(s));
            }

            reset();
        }
	}

	
	public boolean isEqualList(List<Integer> list1, List<Integer> list2){
		List<Integer> one = new ArrayList<Integer>(list1); 
	    List<Integer> two = new ArrayList<Integer>(list2);   
	    Collections.sort(one);
    	Collections.sort(two);      
    	return one.equals(two);
	}

	public void checkCounterStrategy(){
		int length = currWins + currTies + currLoss;
        if(length==4){
            doCounter = true;
            counterTimer = 0;      
        }
        else if (length==5){
            doCounter = true;
            counterTimer = 1; 
        }
        else if (length==6){
            doCounter = true;
            counterTimer = 2; 
        }
	}
	
	private void checkLoss() {
        int lastScoreA = games.get(games.size()-1).playerA.score + games.get(games.size()-2).playerA.score;
        int lastScoreB = games.get(games.size()-1).playerB.score + games.get(games.size()-2).playerB.score;
        int lastScore = lastScoreB - lastScoreA;
        if (isPlayerA) {lastScore = -lastScore;}

        if (lastScore < 0) { //lost last game
            lossStreak ++;
            totalLoss ++;
            currLoss ++;
        } else if (lastScore > 0){ //won
            lossStreak = 0; //reset
            totalWins ++;
            currWins ++;
        } else { //tie
            lossStreak = 0;
            totalTies ++;
            currTies ++;
        }
    }

    private void doLoss() {
        opskills = new ArrayList<List<Integer>>();

        if (isPlayerA) {
            for (int i = 5; i > 0; i -= 2) {
                opskills.add(games.get(games.size()-i).playerB.skills);
            }
        } else {
            for (int i = 5; i > 0; i -= 2) {
                opskills.add(games.get(games.size()-i).playerA.skills);
            }
        }
        
        predOppSkills = new Skills();

        for (int i=0;i<15;i++) {
            List<Integer> temp = new ArrayList<Integer>();
            for (int j = 0; j < 3; j++) {
                temp.add(opskills.get(j).get(i));
            }

            predOppSkills.add(maxmode(temp));
        }
    }

    // public boolean doCounter() {
    // 	return lossStreak >= 3 || (currLoss-2) > currWins;// || ((currTies - 10 > currWins) && totalWins < totalLoss) ;
    // }

    private void reset() {
        lossStreak = 0; // reset, give new strategy a chance to win
        currLoss = 0;
        currWins = 0;
        currTies = 0;
    }

   //  public Skills getCounter() {    	
   //  	//checkCounterStrategy();
   //  	if(immediateCounter){
   //  		List<Integer> opskillsR0 = new ArrayList<Integer>();
   //  		if(!isPlayerA) opskillsR0 = games.get(games.size()-1).playerA.skills;
   //  		else opskillsR0 = games.get(games.size()-1).playerB.skills;
   //  		return new Skills(counter(opskillsR0));
   //  	}
   //  	else if(twoStepCounter){
   //  		List<Integer> ourSkillsR0 = new ArrayList<Integer>();
   //  		if(!isPlayerA) ourSkillsR0 = games.get(games.size()-1).playerB.skills;
			// else ourSkillsR0 = games.get(games.size()-1).playerA.skills;
   //  		if (detectedTwoStep==true){
	  //   		detectedTwoStep = !detectedTwoStep;
   //  			return new Skills(counter(counter(ourSkillsR0)));
   //  		}
   //  		detectedTwoStep = !detectedTwoStep;
   //  		return new Skills(ourSkillsR0);
   //  	}
    	
   //  	lossStreak = 0; // reset, give new strategy a chance to win
   //      currLoss = 0;
   //      currWins = 0;
   //      currTies = 0;

   //      opskills = new ArrayList<List<Integer>>();

   //  	if (isPlayerA) {
   //  		for (int i = 5; i > 0; i -= 2) {
   //  			opskills.add(games.get(games.size()-i).playerB.skills);
   //  		}
   //  	} else {
   //  		for (int i = 5; i > 0; i -= 2) {
   //  			opskills.add(games.get(games.size()-i).playerA.skills);
   //  		}
   //  	}
        
   //      predOppSkills = new Skills();

   //      for (int i=0;i<15;i++) {
   //          List<Integer> temp = new ArrayList<Integer>();
   //          for (int j = 0; j < 3; j++) {
   //          	temp.add(opskills.get(j).get(i));
   //          }

   //          predOppSkills.add(maxmode(temp));
   //      }

   //      //for (int i = 0; i < 3; i++) {
   //      //    List<Integer> cur = opskills.get(i);
   //      //	System.out.println(cur + " : " + predOppSkills.distanceFrom(cur));
   //      //}
   //      System.out.println("predicted:");
   //      System.out.println(predOppSkills);

   //      return new Skills(counter(predOppSkills));
   //  }

    private int maxmode(List<Integer> arr) {
        int maxCount = 0;
        int maxKey = 0;
        Collections.sort(arr);
        int curCount = 0;
        int curKey = arr.get(0);
        for (int i=0; i<arr.size();i++) {
            if (arr.get(i) == curKey) { curCount ++;}
            else {
                if (curCount >= maxCount) {
                    maxCount = curCount;
                    maxKey = curKey;
                }
                curCount = 1;
                curKey = arr.get(i);
            }
        }
        if (curCount >= maxCount) {
            maxCount = curCount;
            maxKey = curKey;
        }

        return maxKey;
    }

    private List<Integer> counter(List<Integer> opponentSkills) {
        Collections.sort(opponentSkills);
        for(int i=0; i<opponentSkills.size();i++ ){
            if (i>=6) {
                opponentSkills.set(i, opponentSkills.get(i)-2);
            }
            else if (i<6) {
                opponentSkills.set(i, opponentSkills.get(i)+3);
            }
        }
        Collections.sort(opponentSkills);
        int sum = opponentSkills.stream().mapToInt(Integer::intValue).sum();
        if(sum>90){
            int difference = sum-90;
            int i = opponentSkills.size() - 1;
            while(difference != 0){
                i--;
                if(i<0) i = opponentSkills.size()-1;
                if(opponentSkills.get(i)>=9 && opponentSkills.get(i)>2) continue;
                else{
                	opponentSkills.set(i,opponentSkills.get(i)-1);
                    difference-=1;
                }
            }
        }
        else if (sum<90) {
            int difference = 90-sum;
            int i=0;
            while(difference != 0){
                i++;
                if(i==opponentSkills.size()) i = 0;
                if(opponentSkills.get(i)<3 && opponentSkills.get(i)<11) continue;
                else{
                	opponentSkills.set(i,opponentSkills.get(i)+1);
                    difference-=1;
                }
            }
        }
        return opponentSkills;
    }

    private boolean isConverging() {
        int total = 0;

        for (int i = 0; i < 3; i++) {
            List<Integer> cur = opskills.get(i);
            total += predOppSkills.distanceFrom(opskills.get(i));
        }

        return total < 15;
    }
}