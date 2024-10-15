package evcomp.w2_greedy_regret_heuristics;

import evcomp.utils.InputGenerator;
import evcomp.utils.Node;
import evcomp.utils.SolutionSaver;
import evcomp.utils.Evaluator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

// Greedy 2-regret heuristics
public class Greedy2RegretMethod {
    // 1. version
    // regret - difference 2nd best position and best position (basic)
    // take node with highest MAX regret MIN change(?)
    private static Random RANDOM;

    public static void main(String[] args) {
        // Args
        if (args.length != 2) {
            System.out.println("Usage: java Greedy2RegretMethod <instance> <n_experiments>");
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
        // seed
        RANDOM = new Random(222);


        // prepare input
        InputGenerator inputGenerator = new InputGenerator(instance);
        int[][] distanceMatrix = inputGenerator.getDistanceMatrix();
        List<Node> nodeList = inputGenerator.getNodeList();

        // utils
        SolutionSaver solutionSaver = new SolutionSaver("w2_greedy_regret_heuristics", "Greedy2RegretMethod", instance);
        Evaluator evaluator = new Evaluator();

        // Perform experiment
        for (int i = 1; i <= nExperiments; i++) {
            long startTime = System.currentTimeMillis();

            List<Integer> solution = generateGreedy2RegretSolution(nodeList, distanceMatrix);

            long timeTaken = System.currentTimeMillis() - startTime;

            int totalCost = evaluator.calculateTotalCost(solution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(solution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            solutionSaver.saveSolution(solution, i, timeTaken, totalCost, totalDistance, objFuncValue);
        }
    }

    private static List<Integer> generateGreedy2RegretSolution(List<Node> nodeList, int[][] distanceMatrix) {
        List<Integer> currCycle = new ArrayList<>();
        int totalNodes = nodeList.size();
        int numberToSelect = totalNodes / 2; // Select 50% of nodes

        boolean[] visited = new boolean[totalNodes];

        // Select randomly the starting vertex
        int startNodeIndex = RANDOM.nextInt(totalNodes);
        currCycle.add(startNodeIndex);
        visited[startNodeIndex] = true;

        // Choose the nearest vertex and create an incomplete cycle from these two vertices
        int nearestNodeIndex = -1;
        int nearestDistance = Integer.MAX_VALUE;
        for (int j = 0; j < totalNodes; j++) {
            if (!visited[j]) {
                int distanceToNewNode = distanceMatrix[startNodeIndex][j] + nodeList.get(j).getCost();
                if (distanceToNewNode < nearestDistance) {
                    nearestDistance = distanceToNewNode;
                    nearestNodeIndex = j;
                }
            }
        }
        // add it to currCycle
        currCycle.add(nearestNodeIndex);
        visited[nearestNodeIndex] = true;

        // repeat
        //  insert into the current cycle in the best possible place the vertex
        //  causing the smallest increase in cycle length
        // until all vertices have been added
        while (currCycle.size() < numberToSelect) {
            // best position
            int bestNode1Index = -1;
            int best1Increase = Integer.MAX_VALUE;
            int best1Position = -1;
            int increase1Change = Integer.MAX_VALUE;

            // second best position
            int bestNode2Index = -1;
            int best2Increase = Integer.MAX_VALUE;
            int best2Position = -1;
            int increase2Change = Integer.MAX_VALUE;

            // all unvisited nodes
            for (int j = 0; j < totalNodes; j++) {
                if (!visited[j]) {
                    int newNode = j;
                    int bestPlace = -1;
                    int bestInc = Integer.MAX_VALUE;
                    int secondInc = Integer.MAX_VALUE;

                    // **every** position in the current cycle is possible place
                    for (int pos = 0; pos < currCycle.size(); pos++) {
                        int prevNode = currCycle.get(pos);
                        int nextNode = currCycle.get((pos + 1) % currCycle.size());

                        // the cost of inserting the new node between prevNode and nextNode
                        int increase = distanceMatrix[prevNode][newNode] // A -> new
                                + distanceMatrix[newNode][nextNode] //  new -> B
                                - distanceMatrix[prevNode][nextNode] // remove previous A->B
                                + nodeList.get(newNode).getCost(); // cost of new node

                        if (increase < bestInc) {
                            secondInc = bestInc;
                            bestInc = increase;
                            bestPlace = pos+1;
                        } else if (increase<secondInc) {
                            secondInc = increase;
                        }
                    }
                    int regret = secondInc-bestInc;
                    if (bestInc<best1Increase){
                        bestNode2Index = bestNode1Index;
                        best2Increase = best1Increase;
                        best2Position = best1Position;
                        increase2Change = increase1Change;
                        bestNode1Index = j;
                        best1Increase = bestInc;
                        best1Position = bestPlace;
                        increase1Change = regret;
                    } else if (bestInc<best2Increase) {
                        bestNode2Index = j;
                        best2Increase = bestInc;
                        best2Position = bestPlace;
                        increase2Change = regret;
                    }
                }
            }

            // insert the best node found into the best position
            if (increase1Change<increase2Change) {
                currCycle.add(best2Position, bestNode2Index);
                visited[bestNode2Index] = true;
            } else {
                currCycle.add(best1Position, bestNode1Index);
                visited[bestNode1Index] = true;
            }
        }
        return currCycle;
    }
}