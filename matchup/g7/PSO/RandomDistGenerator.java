package matchup.g7.PSO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Collections;

public class RandomDistGenerator {
    private static ArrayList<Integer> skills;
    private static ArrayList<Integer> elevens;
    private static HashMap<Integer, Integer> players_skills;

    private static int seed = 0;
    private static Random rand;

    // TESTING
    public static void main(String[] args) {
    //public static ArrayList<Integer> randomDist() {
    	
        Integer max;
        Integer player;
        Integer skill;

        rand = new Random();

        players_skills = new HashMap<Integer, Integer>();
        for (int i = 0; i < 15; i++) {
            players_skills.put(i,1);
        }

        skills = new ArrayList<Integer>();
        elevens = new ArrayList<Integer>();
        for (int i = 0; i < 74; i++) {
            max = 15 - skills.size();
            player = rand.nextInt(15);

            for (int p : elevens) {
                if (player == p) {
                    player++;
                    if (player > 14)
                    	player = 0;
                }
            }

            skill = players_skills.get(player);
            players_skills.put(player, ++skill);

            if (skill == 10) {
                players_skills.remove(player);
                skills.add(11);
                elevens.add(player);
                Collections.sort(elevens);
            }

        }

        for (int i : players_skills.values()) {
            skills.add(i);
        }
        
        
        int n = 0;
        for (int i : skills) {
            System.out.printf("%d\n", i);
            n += i;
        }
        System.out.printf("%d\n", n);
        
        

        //return skills;
    }

}


