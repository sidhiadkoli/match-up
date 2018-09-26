#!/usr/bin/env bash
#
# Example:
#   $ cp ./test_one.sh ../../
#   $ ./test_one.sh 100

ROUNDS=$1

#for OPP in `echo g1 g2 g3 g4 g5 g6 g8 random`;
for OPP in `echo g5`;
do
    WINS=0
    TIES=0
    LOSSES=0

    printf "\nResults against $OPP over $ROUNDS games:\n\n"

    for i in `seq 1 $ROUNDS`;
    do
        #RESULT=$(java matchup.sim.Simulator -p g7 $OPP -n 1 | sed '49q;d' | awk 'NR==1 {print $2}')
        RESULT=$(java matchup.sim.Simulator -p g7 $OPP -n 1 | tail -16 | head -2)
        G7=$(echo $RESULT | awk 'NR==1 {print $2}')
        OTHER=$(echo $RESULT | awk 'NR==1 {print $4}')
        if [ "$G7" == "1" ];
        then
            WINS=$((WINS+1))
        else
            if [ "$OTHER" == "0" ];
            then
                TIES=$((TIES+1))
            else
                LOSSES=$((LOSSES+1))
            fi
        fi
    done

    printf "WINS: $WINS\n"
    printf "TIES: $TIES\n"
    printf "LOSSES: $LOSSES\n\n"
done


