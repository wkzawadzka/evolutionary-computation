package evcomp.w3_local_search;


import evcomp.utils.Evaluator;
import evcomp.utils.InputGenerator;
import evcomp.utils.Node;
import evcomp.utils.SolutionSaver;
import evcomp.w2_greedy_regret_heuristics.Greedy2RegretWeightedSum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Steepest2nodesGreedy {
    public static void main(String[] args) {
        // Args
        if (args.length != 2) {
            System.out.println("Usage: java Steepest2nodesGreedy <instance> <n_experiments>");
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

        // Initialize InputGenerator with given instance
        InputGenerator inputGenerator = new InputGenerator(instance);
        // Retrieve the distance matrix and node list from InputGenerator
        int[][] distanceMatrix = inputGenerator.getDistanceMatrix();
        List<Node> nodeList = inputGenerator.getNodeList();

        SolutionSaver solutionSaver = new SolutionSaver("w3_local_search", "Steepest2nodesGreedy", instance);
        Evaluator evaluator = new Evaluator();

        // Perform experiment
        for (int i = 1; i <= nExperiments; i++) {
            long startTime = System.currentTimeMillis();

            List<Integer> bestStart = Greedy2RegretWeightedSum.generateGreedy2RegretWeightedSumSolution(nodeList, distanceMatrix);
            List<Integer> greedySolution = generateSteepest2nodesRandom(nodeList, distanceMatrix, evaluator, bestStart);

            long timeTaken = System.currentTimeMillis() - startTime;

            int totalCost = evaluator.calculateTotalCost(greedySolution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(greedySolution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            solutionSaver.saveSolution(greedySolution, i, timeTaken, totalCost, totalDistance, objFuncValue);
        }

    }
    private static List<Integer> generateSteepest2nodesRandom(List<Node> nodeList, int[][] distanceMatrix, Evaluator eval, List<Integer> start) {
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

        boolean go = true;
        while(go){
            List<Integer> bestsol = new ArrayList<>();
            int bestchange = Integer.MAX_VALUE;
            for (int i = 0; i < numberToSelect; i++){
                for (int j = 0; j < totalNodes; j++){
                    int idj = nodeList.get(j).getId();
                    List<Integer> swapped = new ArrayList<>(selectedIds);
                    if (!selectedIds.contains(idj)){
                        swapped.set(i, idj);
                    }
                    else{
                        Collections.swap(swapped, i, selectedIds.indexOf(idj));
                    }

                    int change = eval.calculateTotalCost(swapped, nodeList) + eval.calculateTotalDistance(swapped, distanceMatrix);
                    if (change<bestchange){
                        bestchange = change;
                        bestsol = swapped;
                    }
                }
            }
            //System.out.println(bestchange);

            if (bestchange<selectedCost){
                selectedIds = bestsol;
                selectedCost = bestchange;
            }
            else {
                go = false;
            }
        }

        return selectedIds;
    }
}
