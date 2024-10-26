package evcomp.w3_local_search;

import evcomp.utils.Evaluator;
import evcomp.utils.InputGenerator;
import evcomp.utils.Node;
import evcomp.utils.SolutionSaver;
import evcomp.w2_greedy_regret_heuristics.Greedy2RegretWeightedSum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Greedy2edgesGreedy {
    private static Random RANDOM;
    public static void main(String[] args) {
        // Args
        if (args.length != 2) {
            System.out.println("Usage: java Greedy2edgesGreedy <instance> <n_experiments>");
            return;
        }
        String instance = args[0];
        int nExperiments;
        try {
            nExperiments = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Error: n_experiments must be an integer.");
            return;
        }

        // Initialize the Random instance with the seed
        RANDOM = new Random(222);

        // Initialize InputGenerator with given instance
        InputGenerator inputGenerator = new InputGenerator(instance);
        // Retrieve the distance matrix and node list from InputGenerator
        int[][] distanceMatrix = inputGenerator.getDistanceMatrix();
        List<Node> nodeList = inputGenerator.getNodeList();

        SolutionSaver solutionSaver = new SolutionSaver("w3_local_search", "Greedy2edgesGreedy", instance);
        Evaluator evaluator = new Evaluator();


        // Perform experiment
        for (int i = 1; i <= nExperiments; i++) {
            long startTime = System.currentTimeMillis();

            List<Integer> bestStart = Greedy2RegretWeightedSum.generateGreedy2RegretWeightedSumSolution(nodeList, distanceMatrix);
            List<Integer> greedySolution = generateGreedy2edgesGreedy(nodeList, distanceMatrix, evaluator, bestStart);

            long timeTaken = System.currentTimeMillis() - startTime;

            int totalCost = evaluator.calculateTotalCost(greedySolution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(greedySolution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            solutionSaver.saveSolution(greedySolution, i, timeTaken, totalCost, totalDistance, objFuncValue);
        }

    }
    private static List<Integer> generateGreedy2edgesGreedy(List<Node> nodeList, int[][] distanceMatrix, Evaluator eval, List<Integer> start) {
        List<Integer> selectedIds = new ArrayList<>(start);
        int totalNodes = nodeList.size();
        int numberToSelect = totalNodes / 2; // Select 50%

        // Create a list of node IDs to choose from
        List<Integer> ids = new ArrayList<>();
        for (Node node : nodeList) {
            ids.add(node.getId());
        }


        int selectedCost = eval.calculateTotalCost(selectedIds, nodeList) + eval.calculateTotalDistance(selectedIds, distanceMatrix);
        // System.out.println(selectedIds);
        // System.out.println(selectedCost);
        int lastCost = -1;

        boolean go = true;
        while(go){
            List<Integer> longList = new ArrayList<>(ids);
            java.util.Collections.shuffle(longList, RANDOM);
            List<Integer> shortList = new ArrayList<>(selectedIds);
            java.util.Collections.shuffle(shortList, RANDOM);

            findBetter:
            for (int i = 0; i < numberToSelect; i++){
                for (int j = 0; j < totalNodes; j++){
                    int idi = selectedIds.indexOf(shortList.get(i));
                    int idj = ids.indexOf(longList.get(j));
                    List<Integer> swapped = new ArrayList<>(selectedIds);
                    if (!selectedIds.contains(idj)){
                        swapped.set(idi, idj);
                    }
                    else{ // intra - 2 edges
                        int indexIdj = selectedIds.indexOf(idj);
                        // if next to each other - no
                        if (Math.abs(idi - indexIdj) == 1) {
                            continue; 
                        }
                        int start_ = Math.min(idi, indexIdj);
                        int end = Math.max(idi, indexIdj);
                        
                        List<Integer> sublist = swapped.subList(start_, end + 1);
                        Collections.reverse(sublist);
                    }

                    int change = eval.calculateTotalCost(swapped, nodeList) + eval.calculateTotalDistance(swapped, distanceMatrix);
                    if (change<selectedCost){
                        selectedIds = swapped;
                        lastCost = selectedCost;
                        selectedCost = change;
                        break findBetter;
                    }
                }
            }
            if(selectedCost == lastCost){
                go = false;
            }
            else{
                //System.out.println(selectedCost);
                lastCost = selectedCost;
            }

        }

        return selectedIds;
    }
}