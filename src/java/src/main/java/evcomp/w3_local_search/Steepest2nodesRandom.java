package evcomp.w3_local_search;

import evcomp.utils.Evaluator;
import evcomp.utils.InputGenerator;
import evcomp.utils.Node;
import evcomp.utils.SolutionSaver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Steepest2nodesRandom {
    private static Random RANDOM;
    public static void main(String[] args) {
        // Args
        if (args.length != 2) {
            System.out.println("Usage: java Steepest2nodesRandom <instance> <n_experiments>");
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

        SolutionSaver solutionSaver = new SolutionSaver("w3_local_search", "Steepest2nodesRandom", instance);
        Evaluator evaluator = new Evaluator();

        // Perform experiment
        for (int i = 1; i <= nExperiments; i++) {
            long startTime = System.currentTimeMillis();

            List<Integer> randomSolution = generateSteepest2nodesRandom(nodeList, distanceMatrix, evaluator);

            long timeTaken = System.currentTimeMillis() - startTime;

            int totalCost = evaluator.calculateTotalCost(randomSolution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(randomSolution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            solutionSaver.saveSolution(randomSolution, i, timeTaken, totalCost, totalDistance, objFuncValue);
        }

    }
    private static List<Integer> generateSteepest2nodesRandom(List<Node> nodeList, int[][] distanceMatrix, Evaluator eval) {
        List<Integer> selectedIds = new ArrayList<>();
        int totalNodes = nodeList.size();
        int numberToSelect = totalNodes / 2; // Select 50%

        // Create a list of node IDs to choose from
        List<Integer> ids = new ArrayList<>();
        for (Node node : nodeList) {
            ids.add(node.getId());
        }

        // Shuffle the IDs using the seeded random instance
        java.util.Collections.shuffle(ids, RANDOM);
        for (int i = 0; i < numberToSelect; i++) {
            selectedIds.add(ids.get(i));
        }

        int selectedCost = eval.calculateTotalCost(selectedIds, nodeList) + eval.calculateTotalDistance(selectedIds, distanceMatrix);
        System.out.println(selectedIds);
        System.out.println(selectedCost);

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
            System.out.println(bestchange);

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
