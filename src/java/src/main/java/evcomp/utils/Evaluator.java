package evcomp.utils;

import java.util.List;

public class Evaluator {

    public int calculateTotalCost(List<Integer> solution, List<Node> nodeList) {
        int totalCost = 0;
        for (Integer index : solution) {
            totalCost += nodeList.get(index).getCost(); // Sum up the costs of the selected nodes
        }
        return totalCost;
    }

    public int calculateTotalDistance(List<Integer> solution, int[][] distanceMatrix) {
        int totalDistance = 0;
        for (int i = 0; i < solution.size() - 1; i++) {
            totalDistance += distanceMatrix[solution.get(i)][solution.get(i + 1)];
        }
        // Complete the cycle
        totalDistance += distanceMatrix[solution.get(solution.size() - 1)][solution.get(0)];
        return totalDistance;
    }

    public int calculateObjectiveFunction(int totalCost, int totalDistance) {
        return totalCost + totalDistance; // Objective function as the sum of cost and distance
    }
}

