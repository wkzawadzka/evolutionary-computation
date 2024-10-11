package evcomp.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Prepares input to be accessed by optimization methods
public class InputGenerator {
    private int[][] distanceMatrix;
    private List<Node> nodeList;
 
    // Constructor
    public InputGenerator(String instance) {
        if (instance == null) {
            System.out.println("Please provide an instance parameter (A or B) to load the corresponding CSV file.");
            return;
        }

        // Get path to the desired instance CSV file
        String filePath = "";
        if ("A".equalsIgnoreCase(instance)) {
            filePath = "data/input/TSPA.csv"; 
        } else if ("B".equalsIgnoreCase(instance)) {
            filePath = "data/input/TSPB.csv";
        } else {
            System.out.println("Invalid parameter. Please use instance 'A' or 'B'.");
            return;
        }

        // Read nodes from the CSV file
        List<Node_> nodes = readNodesFromCSV(filePath);
        if (nodes == null) {
            System.out.println("Failed to read nodes from the CSV file.");
            return;
        }

        // Generate distance matrix
        this.distanceMatrix = DistanceMatrixGenerator.generateDistanceMatrix(nodes);

        // Generate nodes with id and cost only
        this.nodeList = convertNodeList(nodes);
    }

    /**
     * Reads nodes from a CSV file and returns a list of Node objects.
     *
     * @param filePath The path to the CSV file.
     * @return A list of Node objects, or null if an error occurs.
     */
    private static List<Node_> readNodesFromCSV(String filePath) {
        List<Node_> nodes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Assuming the CSV has three columns: x, y, and cost (e.g., "x,y,cost")
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                if (data.length == 3) {
                    int x = Integer.parseInt(data[0].trim());
                    int y = Integer.parseInt(data[1].trim());
                    int cost = Integer.parseInt(data[2].trim());
                    nodes.add(new Node_(x, y, cost));
                } else {
                    System.out.println("Invalid CSV format. Each line must contain three values (x, y, cost).");
                    return null;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading the CSV file: " + e.getMessage());
            return null;
        }
        return nodes;
    }

    /**
     * Convert List<Node_> to List<Node>, copying cost and generating id 
     * (starting from 0)
     */
    public static List<Node> convertNodeList(List<Node_> nodeList) {
        return IntStream.range(0, nodeList.size())
                        .mapToObj(i -> new Node(i, nodeList.get(i).getCost())) // Generate Node with id and cost
                        .collect(Collectors.toList()); // Collect to a List<Node>
    }

    /**
     * Get the distance matrix.
     *
     * @return The distance matrix (2D array of integers).
     */
    public int[][] getDistanceMatrix() {
        return distanceMatrix;
    }

    /**
     * Get the list of nodes with id and cost.
     *
     * @return The list of nodes with id and cost.
     */
    public List<Node> getNodeList() {
        return nodeList;
    }
}


/**
 * Temporal node class
 * For reading from CSV
 */
class Node_ {
    private int x;
    private int y;
    private int cost;

    public Node_(int x, int y, int cost) {
        this.x = x;
        this.y = y;
        this.cost = cost;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return String.format("Node_[x=%d, y=%d, cost=%d]", x, y, cost);
    }
}

