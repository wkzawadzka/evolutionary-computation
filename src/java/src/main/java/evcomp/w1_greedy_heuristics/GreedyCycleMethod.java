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
            List<Integer> currPath = new ArrayList<>();
            int totalNodes = nodeList.size();
            int numberToSelect = totalNodes / 2; // Select 50% of nodes
            
            boolean[] visited = new boolean[totalNodes];
    
            // Start from the random node
            int startNodeIndex = RANDOM.nextInt(totalNodes);
            currPath.add(startNodeIndex); // path
            visited[startNodeIndex] = true;
            int lastNodeIndex = startNodeIndex; // curr end of path
    
            while (currPath.size() < numberToSelect) {
                int bestNodeIndex = -1;
                int bestIncrease = Integer.MAX_VALUE;
        
                for (int j = 0; j < totalNodes; j++) { // Check all nodes
                    if (!visited[j]) {
                        // calculate increase in obj. val if added 
                        // last_node->new_node + cost(new_node) + new_node->fist_node (cycle)
                        int costToNewNode = nodeList.get(j).getCost();
                        int distanceToNewNode = distanceMatrix[lastNodeIndex][j];
                        int distanceToFirstNode = distanceMatrix[j][currPath.get(0)]; // cycle
        
                        int increase = distanceToNewNode + costToNewNode + distanceToFirstNode;
        
                        // if lower, accept as new best candicate
                        if (increase < bestIncrease) {
                            bestIncrease = increase;
                            bestNodeIndex = j;
                        }
                    }
                }
        
                // add to path
                if (bestNodeIndex != -1) {
                    currPath.add(bestNodeIndex);
                    visited[bestNodeIndex] = true;
                    lastNodeIndex = bestNodeIndex;
                }
            }
            return currPath;
        }
}
