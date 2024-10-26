package evcomp.w1_greedy_heuristics;

import evcomp.utils.InputGenerator;
import evcomp.utils.Node;
import evcomp.utils.SolutionSaver;
import evcomp.utils.Evaluator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class NearNeighborMethod {
    private static Random RANDOM;

    public static void main(String[] args) {
        // Args
        if (args.length != 2) {
            System.out.println("Usage: java NearNeighborMethod <instance> <n_experiments>");
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
        SolutionSaver solutionSaver = new SolutionSaver("w1_greedy_heuristics", "NearNeighborMethod", instance);
        Evaluator evaluator = new Evaluator();

        // Perform experiment
        for (int i = 1; i <= nExperiments; i++) {
            long startTime = System.currentTimeMillis();

            List<Integer> greedySolution = generateNNSolution(nodeList, distanceMatrix);

            long timeTaken = System.currentTimeMillis() - startTime;

            int totalCost = evaluator.calculateTotalCost(greedySolution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(greedySolution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            solutionSaver.saveSolution(greedySolution, i, timeTaken, totalCost, totalDistance, objFuncValue);
        }
    }

    public static List<Integer> generateNNSolution(List<Node> nodeList, int[][] distanceMatrix)  {
        List<Integer> currPath = new ArrayList<>();
        int totalNodes = nodeList.size();
        int numberToSelect = (int) Math.ceil(totalNodes / 2.0); // Select 50% of nodes

        boolean[] visited = new boolean[totalNodes];

        // Start from the random node
        int startNodeIndex = RANDOM.nextInt(totalNodes);
        currPath.add(startNodeIndex); // path
        visited[startNodeIndex] = true;

        //  looks for the nearest neighbor at each position in the current path during each iteration
        while (currPath.size() < numberToSelect) {
            // Loop Over Current Path and Find the Nearest Neighbor:
            // (looking at each position in the current path)
            for (int i=0; i<currPath.size(); i++){
                while (currPath.size() < numberToSelect){
                    int currentNode = currPath.get(i);
                    int bestNodeIndex = -1;
                    int lowestChangeObjVal = Integer.MAX_VALUE;

                    // Find the Closest Unvisited Node
                    for (int j = 0; j < totalNodes; j++) { // Check all nodes
                        if (!visited[j]) {
                            int changeObjVal = distanceMatrix[currentNode][j] + nodeList.get(j).getCost();

                            // if lower, accept as new best candicate
                            if (changeObjVal < lowestChangeObjVal) {
                                lowestChangeObjVal = changeObjVal;
                                bestNodeIndex = j;
                            }
                        }
                    }
                    // add to path
                    if (bestNodeIndex != -1) {
                        currPath.add(i+1, bestNodeIndex);
                        visited[bestNodeIndex] = true;
                    }
                }
            }



        }
        return currPath;
    }
}
