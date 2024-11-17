package evcomp.w3_local_search;

import evcomp.utils.Evaluator;
import evcomp.utils.InputGenerator;
import evcomp.utils.Node;
import evcomp.utils.SolutionSaver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Steepest2edgesRandom {
    private static Random RANDOM;
    private static Evaluator evaluator;

    public static void main(String[] args) {
        // Args
        if (args.length != 2) {
            System.out.println("Usage: java Steepest2edgesRandom <instance> <n_experiments>");
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
        // Initialize evaluator
        evaluator = new Evaluator();
        // Retrieve the distance matrix and node list from InputGenerator
        int[][] distanceMatrix = inputGenerator.getDistanceMatrix();
        List<Node> nodeList = inputGenerator.getNodeList();

        RANDOM = new Random(222);

        // Create a list of node IDs to choose from
        List<Integer> ids = new ArrayList<>();
        for (Node node : nodeList) {
            ids.add(node.getId());
        }

        SolutionSaver solutionSaver = new SolutionSaver("w3_local_search", "Steepest2edgesRandom", instance);

        // Perform experiment
        for (int i = 1; i <= nExperiments; i++) {
            List<Integer> initialSolution = generateInitialRandomSolution(ids, nodeList, distanceMatrix);
            long startTime = System.currentTimeMillis();

            List<Integer> randomSolution = generateSteepest2edgesRandom(initialSolution, nodeList, distanceMatrix);

            long timeTaken = System.currentTimeMillis() - startTime;

            int totalCost = evaluator.calculateTotalCost(randomSolution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(randomSolution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            solutionSaver.saveSolution(randomSolution, i, timeTaken, totalCost, totalDistance, objFuncValue);
        }

    }

    private static List<Integer> generateInitialRandomSolution(List<Integer> ids, List<Node> nodeList, int[][] distanceMatrix) {
        List<Integer> bestSolution = null;
        int lowestObjectiveValue = Integer.MAX_VALUE;

        int totalNodes = nodeList.size();
        int numberToSelect = totalNodes / 2; // Select 50%

        // 200 randomly generated solutions
        for (int i = 0; i < 200; i++) {
            Collections.shuffle(ids, RANDOM);

            List<Integer> selectedIds = new ArrayList<>();
            for (int j = 0; j < numberToSelect; j++) {
                selectedIds.add(ids.get(j));
            }

            // Evaluate the objective function
            int totalCost = evaluator.calculateTotalCost(selectedIds, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(selectedIds, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            if (objFuncValue < lowestObjectiveValue) {
                lowestObjectiveValue = objFuncValue;
                bestSolution = new ArrayList<>(selectedIds); 
            }
        }

        return bestSolution;
    }

    private static List<Integer> generateSteepest2edgesRandom(List<Integer> initialSolution, List<Node> nodeList, int[][] distanceMatrix) {
        int totalNodes = nodeList.size();
        int numberToSelect = totalNodes / 2; // Select 50%

        List<Integer> selectedIds = initialSolution;
        int selectedCost = evaluator.calculateTotalCost(selectedIds, nodeList) + evaluator.calculateTotalDistance(selectedIds, distanceMatrix);

        boolean go = true;
        while(go){
            List<Integer> bestsol = new ArrayList<>();
            int bestchange = Integer.MAX_VALUE;
            for (int i = 0; i < numberToSelect; i++){
                for (int j = 0; j < totalNodes; j++){
                    int idj = nodeList.get(j).getId();
                    int delta = 0;
                    List<Integer> swapped = new ArrayList<>(selectedIds);
                    if (!selectedIds.contains(idj)){
                        int prevNode1 = (i > 0) ? selectedIds.get(i - 1) : selectedIds.get(numberToSelect-1);
                        int nextNode1 = (i < selectedIds.size() - 1) ? selectedIds.get(i + 1) : selectedIds.get(0);
        
                        swapped.set(i, idj);
        
                        int oldCost = distanceMatrix[prevNode1][selectedIds.get(i)]
                                    + distanceMatrix[selectedIds.get(i)][nextNode1] 
                                    + nodeList.get(selectedIds.get(i)).getCost();
        
                        int newCost = distanceMatrix[prevNode1][idj]
                                    + distanceMatrix[idj][nextNode1]
                                    + nodeList.get(idj).getCost();
        
                        delta =  newCost - oldCost;
                    }
                    else{ // intra - 2 edges
                        int indexIdj = selectedIds.indexOf(idj);
                        int start_ = Math.min(i, indexIdj);
                        int end = Math.max(i, indexIdj);

                        // if next to each other - no
                        if (Math.abs(i - indexIdj) <= 1 || (start_ == 0 && end == numberToSelect - 1)) {
                            continue;
                        }
                        
                        // before swapping, get the nodes connected to the two edges (before swap)
                        int prevNode1 = selectedIds.get((start_ - 1 + numberToSelect) % numberToSelect);  // the node before the start of the first edge
                        int nextNode1 =  selectedIds.get((end + 1) % numberToSelect);  // the node after the end of the second edge


                        List<Integer> sublist = swapped.subList(start_, end + 1);
                        Collections.reverse(sublist);

                        int oldDistance = distanceMatrix[prevNode1][selectedIds.get(start_)] + distanceMatrix[selectedIds.get(end)][nextNode1];
                        int newDistance = distanceMatrix[prevNode1][selectedIds.get(end)] + distanceMatrix[selectedIds.get(start_)][nextNode1];

                        delta = newDistance - oldDistance;
                    }

                    if (delta<bestchange){
                        //System.out.println("[i = " + i + "] Curr selected cost: " + selectedCost);
                        //System.out.println("Curr delta: " + delta);
                        //System.out.println("Curr bestchange: " + bestchange);
                        bestchange = delta;
                        bestsol = swapped;
                    }
                }
            }

            // Steepest:
            if (selectedCost+bestchange<selectedCost){
                selectedIds = bestsol;
                selectedCost = selectedCost+bestchange;
            }
            else {
                go = false;
            }
        }

        return selectedIds;
    }
}
