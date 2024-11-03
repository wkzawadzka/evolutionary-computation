package evcomp.w4_candidate_moves;

import evcomp.utils.Evaluator;
import evcomp.utils.InputGenerator;
import evcomp.utils.Node;
import evcomp.utils.SolutionSaver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Comparator;

public class Steepest2edgesCandidate {
    private static Random RANDOM;
    public static void main(String[] args) {
        // Args
        if (args.length != 2) {
            System.out.println("Usage: java Steepest2edgesCandidate <instance> <n_experiments>");
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

        SolutionSaver solutionSaver = new SolutionSaver("w4_candidate_moves", "Steepest2edgesCandidate", instance);
        Evaluator evaluator = new Evaluator();

        // Perform experiment
        for (int i = 1; i <= nExperiments; i++) {
            long startTime = System.currentTimeMillis();

            List<Integer> randomSolution = generateSteepest2edgesCandidate(nodeList, distanceMatrix, evaluator);

            long timeTaken = System.currentTimeMillis() - startTime;

            int totalCost = evaluator.calculateTotalCost(randomSolution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(randomSolution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            solutionSaver.saveSolution(randomSolution, i, timeTaken, totalCost, totalDistance, objFuncValue);
        }

    }
    private static List<Integer> generateSteepest2edgesCandidate(List<Node> nodeList, int[][] distanceMatrix, Evaluator eval) {
        List<Integer> selectedIds = new ArrayList<>();
        int totalNodes = nodeList.size();
        int numberToSelect = totalNodes / 2; // Select 50%

        // Create a list of node IDs to choose from
        List<Integer> ids = new ArrayList<>();
        for (Node node : nodeList) {
            ids.add(node.getId());
        }

        // Shuffle the IDs using the seeded random instance
        // Random initial solution:
        java.util.Collections.shuffle(ids, RANDOM);
        for (int i = 0; i < numberToSelect; i++) {
            selectedIds.add(ids.get(i));
        }

        //create matrix of 10 closest neighbors for each node
        int[][] candidates = new int[totalNodes][10];
        for (int i = 0; i < totalNodes; i++) {
            List<NodeDistance> distances = new ArrayList<>();

            for (int j = 0; j < totalNodes; j++) {
                if (i != j) {
                    double totalDistance = distanceMatrix[i][j] + nodeList.get(j).getCost();
                    distances.add(new NodeDistance(j, totalDistance));
                }
            }
            distances.sort(Comparator.comparingDouble(NodeDistance::getTotalDistance));

            // Add the 10 closest nodes to the matrix
            for (int k = 0; k < 10; k++) {
                candidates[i][k] = distances.get(k).getNodeId();
            }
        }

        boolean go = true;
        while(go){
            List<Integer> bestsol = new ArrayList<>();
            int bestchange = 0;
            int change = 0;
            for (int i = 0; i < numberToSelect; i++){
                for (int j = 0; j < 10; j++){
                    int idi = selectedIds.get(i);
                    int idj = candidates[idi][j];

                    if (!selectedIds.contains(idj)){
                        int prev = -1;
                        int prev2 = -1;
                        if (i != 0) {
                            prev = selectedIds.get(i - 1);
                            if (i!=1){
                                prev2 = selectedIds.get(i - 2);
                            }
                            else{
                                prev2 = selectedIds.get(numberToSelect-1);
                            }
                        }
                        else {
                            prev = selectedIds.get(numberToSelect-1);
                            prev2 = selectedIds.get(numberToSelect-2);
                        }
                        int increase1 = distanceMatrix[idi][idj] // A -> new
                                + distanceMatrix[idj][prev2] //  new -> B
                                - distanceMatrix[idi][prev] // remove previous A->B
                                - distanceMatrix[prev][prev2]
                                - nodeList.get(prev).getCost()
                                + nodeList.get(idj).getCost();


                        int next = selectedIds.get((i+1)%(numberToSelect-1));
                        int next2 = selectedIds.get((i+2)%(numberToSelect-1));
                        int increase2 = distanceMatrix[idi][idj] // A -> new
                                + distanceMatrix[idj][next2] //  new -> B
                                - distanceMatrix[idi][next] // remove previous A->B
                                - distanceMatrix[next][next2]
                                - nodeList.get(next).getCost()
                                + nodeList.get(idj).getCost();

                        if (increase1 < bestchange) {
                            List<Integer> swapped = new ArrayList<>(selectedIds);
                            if (i==0){
                                swapped.set(numberToSelect-1, idj);
                            } else{
                                swapped.set(i-1, idj);
                            }
                            bestsol = swapped;
                            bestchange = increase1;
                            change = 1;
                        }
                        if (increase2 < bestchange) {
                            List<Integer> swapped = new ArrayList<>(selectedIds);
                            swapped.set((i+1)%(numberToSelect-1), idj);
                            bestsol = swapped;
                            bestchange = increase2;
                            change = 2;
                        }
                    }
                    else{ // intra - 2 edges
                        int indexIdj = selectedIds.indexOf(idj);
                        int start_ = Math.min(i, indexIdj);
                        int end = Math.max(i, indexIdj);
                        // if next to each other - no
                        if (Math.abs(i - indexIdj) == 1) {
                            continue;
                        }
                        if (start_==0 & end == numberToSelect-1){
                            continue;
                        }
                        int nexti, previ, nextj, prevj;
                        nexti = selectedIds.get((i+1)%(numberToSelect-1));
                        nextj = selectedIds.get((indexIdj+1)%(numberToSelect-1));
                        if (i != 0) {
                            previ = selectedIds.get(i - 1);
                        }
                        else {
                            previ = selectedIds.get(numberToSelect-1);
                        }
                        if (indexIdj != 0) {
                            prevj = selectedIds.get(indexIdj - 1);
                        }
                        else {
                            prevj = selectedIds.get(numberToSelect-1);
                        }

                        int increase1 = distanceMatrix[idi][idj]
                                - distanceMatrix[idi][nexti]
                                + distanceMatrix[nexti][nextj]
                                -distanceMatrix[nextj][idj];
                        int increase2 = distanceMatrix[idi][idj]
                                - distanceMatrix[idi][previ]
                                + distanceMatrix[previ][prevj]
                                -distanceMatrix[prevj][idj];


                        if (increase1 < bestchange) {
                            List<Integer> swapped = new ArrayList<>(selectedIds);
                            List<Integer> sublist = swapped.subList(start_+1, end+1);
                            Collections.reverse(sublist);
                            bestsol = swapped;
                            bestchange = increase1;
                            change=3;
                        }
                        if (increase2 < bestchange) {
                            List<Integer> swapped = new ArrayList<>(selectedIds);
                            List<Integer> sublist = swapped.subList(start_, end);
                            Collections.reverse(sublist);
                            bestsol = swapped;
                            bestchange = increase2;
                            change=4;
                        }
                    }
                }
            }
            System.out.println(bestchange);
            System.out.println(change);
            System.out.println(bestsol);

            // Steepest:
            if (bestchange<0){
                selectedIds = bestsol;
            }
            else {
                go = false;
            }
        }

        return selectedIds;
    }

    static class NodeDistance {
        private final int nodeId;
        private final double totalDistance;

        public NodeDistance(int nodeId, double totalDistance) {
            this.nodeId = nodeId;
            this.totalDistance = totalDistance;
        }

        public int getNodeId() {
            return nodeId;
        }

        public double getTotalDistance() {
            return totalDistance;
        }
    }
}
