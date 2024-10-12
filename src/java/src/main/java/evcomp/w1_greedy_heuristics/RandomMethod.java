package evcomp.w1_greedy_heuristics;

import evcomp.utils.InputGenerator;
import evcomp.utils.Node;
import evcomp.utils.SolutionSaver;
import evcomp.utils.Evaluator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class RandomMethod {
    private static Random RANDOM;
    public static void main(String[] args) {
        // Args
        if (args.length != 2) {
            System.out.println("Usage: java RandomMethod <instance> <n_experiments>");
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

        SolutionSaver solutionSaver = new SolutionSaver("w1_greedy_heuristics", "RandomMethod", instance);
        Evaluator evaluator = new Evaluator();

        // Perform experiment
        for (int i = 1; i <= nExperiments; i++) {
            long startTime = System.currentTimeMillis(); 

            List<Integer> randomSolution = generateRandomSolution(nodeList);

            long timeTaken = System.currentTimeMillis() - startTime;

            int totalCost = evaluator.calculateTotalCost(randomSolution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(randomSolution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            solutionSaver.saveSolution(randomSolution, i, timeTaken, totalCost, totalDistance, objFuncValue);
        }

    }
    private static List<Integer> generateRandomSolution(List<Node> nodeList) {
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

        return selectedIds;
    }
}
