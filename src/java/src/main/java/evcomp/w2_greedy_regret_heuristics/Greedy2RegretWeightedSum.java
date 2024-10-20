package evcomp.w2_greedy_regret_heuristics;

import evcomp.utils.InputGenerator;
import evcomp.utils.Node;
import evcomp.utils.SolutionSaver;
import evcomp.utils.Evaluator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

// Greedy heuristics with a weighted sum criterion – 2-regret + best change of the objective
// function. By default use equal weights but you can also experiment with other values.
public class Greedy2RegretWeightedSum {
    // 2. version
    // weight greedy criterion - change to obj. with regret (take into accound - invert the sign 
    // of the weight e..g of regret, take it with minus, and select min sum of best change of 
    // inverted regret)
    // private static double regretWeight = -1.0; // Weight for regret, negative sign as specified
    // private static double greedyWeight = 1.0;  // Weight for objective function (greedy)
    private static double weight = 0.5;
    private static Random RANDOM;

    public static void main(String[] args) {
        // Args
        if (args.length != 2) {
            System.out.println("Usage: java Greedy2RegretWeightedSumMethod <instance> <n_experiments>");
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
        SolutionSaver solutionSaver = new SolutionSaver("w2_greedy_regret_heuristics", "Greedy2RegretWeightedSumMethod", instance);
        Evaluator evaluator = new Evaluator();

        // Perform experiment
        for (int i = 1; i <= nExperiments; i++) {
            long startTime = System.currentTimeMillis();

            List<Integer> solution = generateGreedy2RegretWeightedSumSolution(nodeList, distanceMatrix);

            long timeTaken = System.currentTimeMillis() - startTime;

            int totalCost = evaluator.calculateTotalCost(solution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(solution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            solutionSaver.saveSolution(solution, i, timeTaken, totalCost, totalDistance, objFuncValue);
        }
    }

    private static List<Integer> generateGreedy2RegretWeightedSumSolution(List<Node> nodeList, int[][] distanceMatrix) {
        List<Integer> currCycle = new ArrayList<>();
        int totalNodes = nodeList.size();
        int numberToSelect = totalNodes / 2; // Select 50% of nodes

        boolean[] visited = new boolean[totalNodes];

        // Select randomly the starting vertex
        int startNodeIndex = RANDOM.nextInt(totalNodes);
        currCycle.add(startNodeIndex);
        visited[startNodeIndex] = true;

        // Find the nearest vertex to start the cycle
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
        currCycle.add(nearestNodeIndex);
        visited[nearestNodeIndex] = true;

        // Repeat until all nodes are added
        while (currCycle.size() < numberToSelect) {
            int bestNodeIndex = -1;
            int bestPosition = -1;
            double bestWeightedSum = Double.MAX_VALUE;

            // Iterate over all unvisited nodes
            for (int j = 0; j < totalNodes; j++) {
                if (!visited[j]) {
                    int newNode = j;
                    int bestInc = Integer.MAX_VALUE;
                    int secondInc = Integer.MAX_VALUE;
                    int bestPlace = -1;

                    // Find the best and second-best position for this node
                    for (int pos = 0; pos < currCycle.size(); pos++) {
                        int prevNode = currCycle.get(pos);
                        int nextNode = currCycle.get((pos + 1) % currCycle.size());

                        // Obj.func. change
                        int increase = distanceMatrix[prevNode][newNode] // A -> new
                                + distanceMatrix[newNode][nextNode] // new -> B
                                - distanceMatrix[prevNode][nextNode]
                                + nodeList.get(newNode).getCost(); // remove previous A -> B

                        if (increase < bestInc) {
                            secondInc = bestInc;
                            bestInc = increase;
                            bestPlace = pos + 1;
                        } else if (increase < secondInc) {
                            secondInc = increase;
                        }
                    }

                    // regret
                    int regret = secondInc - bestInc;

                    // weighted sum: objective function (greedy) + regret (weighted)
                    // bestInc is objective func val
                    double weightedSum = (1-weight) * bestInc + weight * (-1.0 * regret);
                    // Select the node with the smallest weighted sum
                    if (weightedSum < bestWeightedSum) {
                        bestNodeIndex = newNode;
                        bestPosition = bestPlace;
                        bestWeightedSum = weightedSum;
                    }
                }
            }

            // insert selected node into best position
            currCycle.add(bestPosition, bestNodeIndex);
            visited[bestNodeIndex] = true;
        }

        return currCycle;
    }
}
