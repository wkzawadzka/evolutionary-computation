package evcomp.utils;

/**
 * Node class
 */
public class Node {
    private int id;
    private int cost;

    // Constructor to initialize id and cost
    public Node(int id, int cost) {
        this.id = id;
        this.cost = cost;
    }

    // Getter for id
    public int getId() {
        return id;
    }

    // Getter for cost
    public int getCost() {
        return cost;
    }

    // Override toString to display node details
    @Override
    public String toString() {
        return String.format("Node[id=%d, cost=%d]", id, cost);
    }
}
