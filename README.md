# MonteCarloTreeSearch

# Dossier Figures pour les figures de chaque benchmark realisé
At first, run 
```
javac -d classes -cp lib/pddl4j-4.0.0.jar src/fr/uga/pddl4j/examples/mcts/MonteCarloNode.java src/fr/uga/pddl4j/examples/mcts/MonteCarloPlanner.java
```

Then the two bash files : run_tests.sh and run_tests_with_HSP.sh can run automaically all the tests in the folder src/test/* and create the summary file on .csv in the folder results and results_hsp  

so run 
```
chmod +x run_tests.sh
```

```
./run_tests.sh
```
or 
```
chmod +x run_tests_with_HSP.sh
```

```
./run_tests_with_HSP.sh
```