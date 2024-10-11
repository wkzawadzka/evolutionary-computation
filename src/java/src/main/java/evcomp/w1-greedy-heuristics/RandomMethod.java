package evcomp.w1-greedy-heuristics;

import evcomp.utils.InputGenerator;
import evcomp.utils.Node;
import java.util.List;

public class RandomMethod {
    public static void main(String[] args) {

        // Initialize InputGenerator with given instance
        InputGenerator inputGenerator = new InputGenerator("A");
        // Retrieve the distance matrix and node list from InputGenerator
        int[][] distanceMatrix = inputGenerator.getDistanceMatrix();
        List<Node> nodeList = inputGenerator.getNodeList();

        // Check if the data was successfully generated
        if (distanceMatrix != null && nodeList != null) {
            System.out.println("Distance Matrix:");
            for (int i = 0; i < distanceMatrix.length; i++) {
                for (int j = 0; j < distanceMatrix[i].length; j++) {
                    System.out.printf("%d\t", distanceMatrix[i][j]);
                }
                System.out.println();
            }
            System.out.println("\nNode List:");
            nodeList.forEach(System.out::println);
        } else {
            System.out.println("Failed to generate data.");
        }
    }
}
