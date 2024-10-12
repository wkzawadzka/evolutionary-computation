package evcomp.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DistanceMatrixGenerator {

    public static int[][] generateDistanceMatrix(List<Node_> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            System.out.println("The list of nodes is empty or null.");
            return null;
        }

        // Calculate distance matrix
        int[][] distanceMatrix = calculateDistanceMatrix(nodes);

        // // Print :)
        // System.out.println("\nDistance Matrix:");
        // for (int i = 0; i < distanceMatrix.length; i++) {
        //     for (int j = 0; j < distanceMatrix[i].length; j++) {
        //         System.out.printf("%d\t", distanceMatrix[i][j]);
        //     }
        //     System.out.println();
        // }

        return distanceMatrix;
    }

    private static int[][] calculateDistanceMatrix(List<Node_> nodes) {
        int size = nodes.size();
        int[][] distanceMatrix = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    Node_ node1 = nodes.get(i);
                    Node_ node2 = nodes.get(j);

                    // The distances between nodes are calculated as Euclidean distances
                    // rounded mathematically to integer values.
                    int distance = Math.round(
                        (float) Math.sqrt(Math.pow(node2.getX() - node1.getX(), 2) + Math.pow(node2.getY() - node1.getY(), 2))
                    );
                    distanceMatrix[i][j] = distance;
                } else {
                    // Distance to itself is 0
                    distanceMatrix[i][j] = 0;
                }
            }
        }

        return distanceMatrix;
    }
}

