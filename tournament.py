import subprocess, os, sys, shutil
import random

def mkdir(path):
    try:
        os.mkdir(path)
    except:
        pass


root = 'results'

players = ['g1', 'g2', 'g3', 'g4', 'g5', 'g6', 'g7', 'g8', 'random', 'random_permute']
bucket = list(players)

games = []

# Match up players so that each has played first 5 times.
for p in players:
    bucket.remove(p)
    random.shuffle(bucket)

    count = 0
    for i in range(len(bucket)):
        if (p, bucket[i]) not in games and (bucket[i], p) not in games:
            if p not in ['random', 'random_permute'] or bucket[i] not in ['random', 'random_permute']:
                games.append((p, bucket[i]))
                count += 1

        if count == 5:
            break

    bucket.append(p)

for p1 in players:
    for p2 in players:
        if (p1, p2) not in games and (p2, p1) not in games:
            if p1 not in ['random', 'random_permute'] or p2 not in ['random', 'random_permute']:
                games.append((p1, p2))

print(len(games))

for p in players:
    mkdir(root + '/' + p)


for g in games:
    mkdir(root + '/' + g[0] + '/' + g[0] + "_" + g[1])
    mkdir(root + '/' + g[1] + '/' + g[0] + "_" + g[1])

    for n in [100, 1000]:
        output = 'n' + str(n) + '.txt'
        f = open(output, 'w')

        subprocess.run(['java', 'matchup.sim.Simulator', '-p', g[0], g[1], '-n', str(n)],
                        stdout=f, stderr=f)

        f.close()

        shutil.copy(output, root + '/' + g[0] + '/' + g[0] + "_" + g[1] + '/' + output)
        shutil.copy(output, root + '/' + g[1] + '/' + g[0] + "_" + g[1] + '/' + output)

        os.remove(output)

