//package matchup.g7.PSO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Collections;

public class RandomDistGenerator {
    private ArrayList<Integer> skills;
    private ArrayList<Integer> elevens;
    private HashMap<Integer, Integer> players_skills;

    private int seed = 0;
    private Random rand;

    // TESTING
    //public static void main(String[] args) {
    public ArrayList<Integer> randomDist() {
        ArrayList<Integer> skills;
        ArrayList<Integer> elevens;
        HashMap<Integer, Integer> players_skills;

        int seed = 0;
        Random rand;

        Integer max;

        Integer player;
        Integer skill;

        rand = new Random();

        players_skills = new HashMap<Integer, Integer>();
        for (int k = 0; k < 15; k++) {
            players_skills.put(k,1);
        }

        skills = new ArrayList<Integer>();
        elevens = new ArrayList<Integer>();
        for (Integer i = 0; i < 75; i++) {
            max = 15 - skills.size();
            player = rand.nextInt(max);

            for (Integer p : elevens) {
                if (player >= p) {
                    player++;
                }
            }
            skill = players_skills.get(player);
            for (Integer name : players_skills.keySet()) {
                String key = name.toString();
                String value = players_skills.get(name).toString();
            }
            players_skills.put(player, ++skill);

            if (skill == 11) {
                players_skills.remove(player);
                skills.add(11);
                elevens.add(player);
                Collections.sort(elevens);
            }

            /*TESTING
            int n = 0;
            for (int l : players_skills.values()) {
                n += l;
            }
            for (int m : elevens) {
                n += m;
            }
            System.out.printf("%d\n", n);
            */

        }

        for (int j : players_skills.values()) {
            skills.add(j);
        }

        /*TESTING
        int n = 0;
        for (int i : skills) {
            //System.out.printf("%d\n", i);
            n += i;
        }
        System.out.printf("%d\n", n);
        */

        return skills;
    }

}


