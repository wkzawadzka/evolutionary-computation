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

        // create matrix of 10 closest neighbors for each node
        // done just once, at the start
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

            // add the 10 closest nodes to the matrix
            for (int k = 0; k < 10; k++) {
                candidates[i][k] = distances.get(k).getNodeId();
            }
        }

        boolean go = true;
        while(go){
            List<Integer> bestsol = new ArrayList<>();
            int bestchange = 0;
            int change = 0;
            for (int i = 0; i < numberToSelect; i++){ // for each node in curr solution
                for (int j = 0; j < 10; j++){ // for each candidate in candidate moves of given node

                    int idi = selectedIds.get(i); // curr node
                    int idj = candidates[idi][j]; // candidate j

                    // if candidate not in current solution then do:
                    if (!selectedIds.contains(idj)){
                        // 2 node swap: so that candidate now becomes neihgbour of curr node:
                        int prev = selectedIds.get((i - 1 + numberToSelect) % numberToSelect);  // previous node
                        int prev2 = selectedIds.get((i - 2 + numberToSelect) % numberToSelect); // second previous node
                        int next = selectedIds.get((i + 1) % numberToSelect); // next node
                        int next2 = selectedIds.get((i + 2) % numberToSelect); // second next node
                        
                        // move1: -1 -> behind
                        int oldF = distanceMatrix[idi][prev] + distanceMatrix[prev][prev2] + nodeList.get(prev).getCost();
                        int newF = distanceMatrix[idi][idj] + distanceMatrix[idj][prev2] + nodeList.get(idj).getCost();
                        int increase1 = newF - oldF;
                        if (increase1 < bestchange) {
                            List<Integer> swapped = new ArrayList<>(selectedIds);
                            swapped.set((i - 1 + numberToSelect) % numberToSelect, idj); 
                            bestsol = swapped;
                            bestchange = increase1;
                            change = 1;
                        }

                        // move2: +1 -> in front of
                        int oldF2 = distanceMatrix[idi][next] + distanceMatrix[next][next2] + nodeList.get(next).getCost();
                        int newF2 = distanceMatrix[idi][idj] + distanceMatrix[idj][next2] + nodeList.get(idj).getCost();
                        int increase2 = newF2 - oldF2;
                        if (increase2 < bestchange) {
                            List<Integer> swapped = new ArrayList<>(selectedIds);
                            swapped.set((i + 1)%(numberToSelect), idj);
                            bestsol = swapped;
                            bestchange = increase2;
                            change = 2;
                        }
                    }
                    // if candidate in current solution then do:
                    else{ 
                        // intra - 2 edges swap
                        // so that candidate now becomes neihgbour of curr node:
                        int indexIdj = selectedIds.indexOf(idj);
                        int start_ = Math.min(i, indexIdj); // smaller out of currNode & candidate
                        int end = Math.max(i, indexIdj); // larger out of currNode & candidate
                        // if already neighbours then skip
                        if (Math.abs(i - indexIdj) <= 1 || (start_ == 0 && end == numberToSelect - 1)) {
                            continue;
                        }

                        int nexti, previ, nextj, prevj;
                        nexti = selectedIds.get((i + 1) % numberToSelect); 
                        nextj = selectedIds.get((indexIdj + 1) % numberToSelect); 
                        previ = selectedIds.get((i - 1 + numberToSelect) % numberToSelect);
                        prevj = selectedIds.get((indexIdj - 1 + numberToSelect) % numberToSelect); 
                        
                        int increase1 = distanceMatrix[idi][idj]
                                - distanceMatrix[idi][nexti]
                                + distanceMatrix[nexti][nextj]
                                - distanceMatrix[nextj][idj];
                        if (increase1 < bestchange) {
                            List<Integer> swapped = new ArrayList<>(selectedIds);
                            Collections.reverse(swapped.subList(start_ + 1, end + 1));
                            bestsol = swapped;
                            bestchange = increase1;
                            change=3;
                        }

                        int increase2 = distanceMatrix[idi][idj]
                        - distanceMatrix[idi][previ]
                        + distanceMatrix[previ][prevj]
                        - distanceMatrix[prevj][idj];
                        if (increase2 < bestchange) {
                            List<Integer> swapped = new ArrayList<>(selectedIds);
                            Collections.reverse(swapped.subList(start_, end));
                            bestsol = swapped;
                            bestchange = increase2;
                            change=4;
                        }
                    }
                }
            }
            //System.out.println(bestchange);
            //System.out.println(change);
            //System.out.println(bestsol);

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
