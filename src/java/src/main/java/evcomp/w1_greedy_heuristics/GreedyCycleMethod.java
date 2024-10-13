package evcomp.w1_greedy_heuristics;

import evcomp.utils.InputGenerator;
import evcomp.utils.Node;
import evcomp.utils.SolutionSaver;
import evcomp.utils.Evaluator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class GreedyCycleMethod {
    private static Random RANDOM;

    public static void main(String[] args) {
        // Args
        if (args.length != 2) {
            System.out.println("Usage: java GreedyCycleMethod <instance> <n_experiments>");
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
        SolutionSaver solutionSaver = new SolutionSaver("w1_greedy_heuristics", "GreedyCycleMethod", instance);
        Evaluator evaluator = new Evaluator();

        // Perform experiment
        for (int i = 1; i <= nExperiments; i++) {
            long startTime = System.currentTimeMillis();

            List<Integer> greedySolution = generateGreedyCycleSolution(nodeList, distanceMatrix);

            long timeTaken = System.currentTimeMillis() - startTime;

            int totalCost = evaluator.calculateTotalCost(greedySolution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(greedySolution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            solutionSaver.saveSolution(greedySolution, i, timeTaken, totalCost, totalDistance, objFuncValue);
        }
    }

    private static List<Integer> generateGreedyCycleSolution(List<Node> nodeList, int[][] distanceMatrix) {
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
                int distanceToNewNode = distanceMatrix[startNodeIndex][j];
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
            int bestNodeIndex = -1;
            int bestIncrease = Integer.MAX_VALUE;
            int bestPosition = -1;

            // all unvisited nodes
            for (int j = 0; j < totalNodes; j++) {
                if (!visited[j]) {
                    int newNode = j;

                    // **every** position in the current cycle is possible place
                    for (int pos = 0; pos < currCycle.size(); pos++) {
                        int prevNode = currCycle.get(pos);
                        int nextNode = currCycle.get((pos + 1) % currCycle.size()); 

                        // the cost of inserting the new node between prevNode and nextNode
                        int increase = distanceMatrix[prevNode][newNode] // A -> new
                                     + distanceMatrix[newNode][nextNode] //  new -> B
                                     - distanceMatrix[prevNode][nextNode] // remove previous A->B
                                     + nodeList.get(newNode).getCost(); // cost of new node

                        if (increase < bestIncrease) {
                            bestIncrease = increase;
                            bestNodeIndex = newNode;
                            bestPosition = pos + 1;
                        }
                    }
                }
            }

            // insert the best node found into the best position
            if (bestNodeIndex != -1) {
                currCycle.add(bestPosition, bestNodeIndex);
                visited[bestNodeIndex] = true;
            }
        }
        return currCycle;
    }
}