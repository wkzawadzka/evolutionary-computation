package evcomp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Move {
    private String moveType;
    private int node1;
    private int node2;
    private int prevNode;
    private int nextNode;
    private int delta;

    public Move(int node1, int node2, int delta, String type, int prevNode, int nextNode) {
        this.node1 = node1;
        this.node2 = node2;
        this.moveType = type;
        this.delta = delta;
        this.prevNode = prevNode;
        this.nextNode = nextNode;
    }

    public int getNode1() {
        return node1;
    }

    public int getNode2() {
        return node2;
    }

    public int getPrevNode() {
        return prevNode;
    }

    public int getNextNode() {
        return nextNode;
    }

    public int getDelta() {
        return delta;
    }

    public String getType() {
        return moveType;
    }


    public List<Integer> modifySolution(List<Integer> solution){
        List<Integer> swapped = new ArrayList<>(solution);
        int node1_newid = solution.indexOf(node1);
        int node2_newid = solution.indexOf(node2);
        if (moveType.equals("inter")) {
            //System.out.println("node1_newid: " +  node1_newid + " node2: " + node2);
            swapped.set(node1_newid, node2);
        } else if (moveType.equals("intra")) {
            //System.out.println("Revesing from node1_newid: " +  node1_newid + " node2: " + node2_newid);
            Collections.reverse(swapped.subList(node1_newid, node2_newid + 1));
        }
        return(swapped);
    }

    public int checkIfMoveValid(List<Integer> solution, int numberToSelect){
        int output = 1;

        // three cases:
        // A: edges no longer exist in the current solution (at least one of them)
        // B: edges occur in the current solution in a different relative direction from the saved one – not applicable now but the move can be applied in the future
        // C: edges appear in the current solution in the same relative direction (also both reversed)

        // output:
        // 0: invalid (A)
        // 1: valid (C)
        // 2: skip (= leave the move in LM but do not apply it browse LM further) (B)


        if ("inter".equals(moveType)) {
            // nodes not there
            if (!solution.contains(prevNode) || !solution.contains(node1) || !solution.contains(nextNode)
                || solution.contains(node2)) {
                output = 0; // Invalid (A)
                return output;
            }

            // check if edges exist
            int node1_newid = solution.indexOf(node1);
            int prevNode_newid = solution.indexOf(prevNode);
            int nextNode_newid = solution.indexOf(nextNode);
            if (((Math.abs(prevNode_newid - node1_newid) != 1) || (Math.abs(nextNode_newid - node1_newid) != 1))
                && ((Math.abs(prevNode_newid - node1_newid) != numberToSelect-1)) && ((Math.abs(nextNode_newid - node1_newid) != numberToSelect-1))) {
                output = 0; // Invalid (A) - not adjacent anymore
                return output;
            }

            // check if order same 
            if (((prevNode_newid - nextNode_newid) == 2) || ((nextNode_newid - prevNode_newid) == numberToSelect-2)){
                output = 2; // Adjacent but in reversed order, skip (B)
                return output;
            }

            // else valid (C) :)

        } else if ("intra".equals(moveType)) {
            // nodes not there
            if (!solution.contains(prevNode) || !solution.contains(node1) || !solution.contains(nextNode)
                || (!solution.contains(node2))) {
                output = 0; // Invalid (A)
                return output;
            }

            // check if edges exist
            int start_newid = solution.indexOf(node1);
            int end_newid = solution.indexOf(node2);
            int prevNode_newid = solution.indexOf(prevNode); // before start
            int nextNode_newid = solution.indexOf(nextNode); // after end
            if (((Math.abs(prevNode_newid - start_newid) != 1) || (Math.abs(nextNode_newid - end_newid) != 1))
            && ((Math.abs(prevNode_newid - start_newid) != numberToSelect-1)) && ((Math.abs(nextNode_newid - end_newid) != numberToSelect-1))) {
                output = 0; // Invalid (A)
                return output;
            }

            if (((prevNode_newid - start_newid) == 1) ||  ((end_newid - nextNode_newid) == 1) 
            || start_newid > end_newid) {
                output = 2; // in different order, skip (B)
                return output;
            }

             // else valid (C) :)
        }


        return output;
    }
}
