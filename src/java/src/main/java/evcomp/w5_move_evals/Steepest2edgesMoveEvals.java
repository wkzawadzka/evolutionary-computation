package evcomp.w5_move_evals;

import evcomp.utils.Evaluator;
import evcomp.utils.InputGenerator;
import evcomp.utils.Node;
import evcomp.utils.SolutionSaver;
import evcomp.utils.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Steepest2edgesMoveEvals {
    private static Random RANDOM;
    private static Evaluator evaluator;

    public static void main(String[] args) {
        // Args
        if (args.length != 2) {
            System.out.println("Usage: java Steepest2edgesMoveEvals <instance> <n_experiments>");
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

        // Initialize InputGenerator with given instance
        InputGenerator inputGenerator = new InputGenerator(instance);
        // Initialize evaluator
        evaluator = new Evaluator();
        // Retrieve the distance matrix and node list from InputGenerator
        int[][] distanceMatrix = inputGenerator.getDistanceMatrix();
        List<Node> nodeList = inputGenerator.getNodeList();

        RANDOM = new Random(222);

        // Create a list of node IDs to choose from
        List<Integer> ids = new ArrayList<>();
        for (Node node : nodeList) {
            ids.add(node.getId());
        }

        SolutionSaver solutionSaver = new SolutionSaver("w5_move_evals", "Steepest2edgesMoveEvals", instance);

        // Perform experiment
        for (int i = 1; i <= nExperiments; i++) {
            List<Integer> initialSolution = generateInitialRandomSolution(ids, nodeList, distanceMatrix);
            long startTime = System.currentTimeMillis();

            List<Integer> randomSolution = generateSteepest2edgesMoveEvals(initialSolution, nodeList, distanceMatrix);

            long timeTaken = System.currentTimeMillis() - startTime;

            int totalCost = evaluator.calculateTotalCost(randomSolution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(randomSolution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            solutionSaver.saveSolution(randomSolution, i, timeTaken, totalCost, totalDistance, objFuncValue);
        }

    }

    private static List<Integer> generateInitialRandomSolution(List<Integer> ids, List<Node> nodeList, int[][] distanceMatrix) {
        List<Integer> bestSolution = null;
        int lowestObjectiveValue = Integer.MAX_VALUE;

        int totalNodes = nodeList.size();
        int numberToSelect = totalNodes / 2; // Select 50%

        // 200 randomly generated solutions
        for (int i = 0; i < 200; i++) {
            Collections.shuffle(ids, RANDOM);

            List<Integer> selectedIds = new ArrayList<>();
            for (int j = 0; j < numberToSelect; j++) {
                selectedIds.add(ids.get(j));
            }

            // Evaluate the objective function
            int totalCost = evaluator.calculateTotalCost(selectedIds, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(selectedIds, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            if (objFuncValue < lowestObjectiveValue) {
                lowestObjectiveValue = objFuncValue;
                bestSolution = new ArrayList<>(selectedIds); 
            }
        }

        return bestSolution;
    }


    private static List<Integer> generateSteepest2edgesMoveEvals(List<Integer> initialSolution, List<Node> nodeList, int[][] distanceMatrix) {
        int totalNodes = nodeList.size();
        int numberToSelect = totalNodes / 2; // Select 50%


        // Initiate LM – a list of moves that bring improvement ordered from the best to the worst
        // List of improving moves sorter according to the delta
        SortedSet<Move> LM = new TreeSet<>(Comparator
                .comparingInt(Move::getDelta)
                .thenComparing(Move::getNode1)    // Compare by node1 if deltas are equal
                .thenComparing(Move::getNode2)  // Compare by node2 if deltas, and node1 are equal
                .thenComparing(Move::getNextNode)); // & then by enighbor 
        // Generate an initial solution x
        List<Integer> selectedIds = initialSolution;

        // Initialize affectedNodes set, initially all 
        Set<Integer> affectedNodes = new HashSet<>(selectedIds);

        boolean go = true;
        while(go){
            go = false;

            // Evaluate all new moves and add improving moves to LM
            for (int nodeI : affectedNodes){
                int i = selectedIds.indexOf(nodeI);
                for (int j = 0; j < totalNodes; j++){
                    int idj = nodeList.get(j).getId();
                    int delta = 0;

                    if (!selectedIds.contains(idj)){
                        // Inter-route move: swap nodes
                        int prevNode1 = (i > 0) ? selectedIds.get(i - 1) : selectedIds.get(numberToSelect-1);
                        int nextNode1 = (i < selectedIds.size() - 1) ? selectedIds.get(i + 1) : selectedIds.get(0);
    
                        int oldCost = distanceMatrix[prevNode1][selectedIds.get(i)]
                                    + distanceMatrix[selectedIds.get(i)][nextNode1] 
                                    + nodeList.get(selectedIds.get(i)).getCost();
        
                        int newCost = distanceMatrix[prevNode1][idj]
                                    + distanceMatrix[idj][nextNode1]
                                    + nodeList.get(idj).getCost();
    
                        delta = newCost - oldCost;
                        if (delta < 0 ){
                            LM.add(new Move(selectedIds.get(i), idj, delta, "inter", prevNode1, nextNode1));
                            // When evaluating new moves we need to consider also moves with inverted edges (same delta)
                            LM.add(new Move(selectedIds.get(i), idj, delta, "inter", nextNode1, prevNode1));
                        }
                    }
                    else {
                        // Intra-route move: swap edges
                        int indexIdj = selectedIds.indexOf(idj);
                        int start_ = Math.min(i, indexIdj);
                        int end = Math.max(i, indexIdj);

                        if (Math.abs(i - indexIdj) <= 1 || (start_ == 0 && end == numberToSelect - 1)) {
                            continue;
                        }

                        // normal
                        int prevNode1 = selectedIds.get((start_ - 1 + numberToSelect) % numberToSelect);
                        int nextNode1 = selectedIds.get((end + 1) % numberToSelect);

                        int oldDistance = distanceMatrix[prevNode1][selectedIds.get(start_)] + distanceMatrix[selectedIds.get(end)][nextNode1];
                        int newDistance = distanceMatrix[prevNode1][selectedIds.get(end)] + distanceMatrix[selectedIds.get(start_)][nextNode1];
                        delta = newDistance - oldDistance;
                        if (delta < 0 ){
                            LM.add(new Move(selectedIds.get(start_), selectedIds.get(end), delta, "intra", prevNode1, nextNode1));
                            // When evaluating new moves we need to consider also moves with inverted edges (same delta)
                            LM.add(new Move(prevNode1, nextNode1, delta, "intra", selectedIds.get(start_), selectedIds.get(end)));
                        }
                        
                    }
                }
            }
            
            // for moves m from LM starting from the best until a applicable move is found
            // browse moves from LM
            List<Move> movesToRemove = new ArrayList<>();
            Set<Integer> newlyAffectedNodes = new HashSet<>();
            
            for (Move move : LM) { // sorted LM (lowest (best) -> highest (worst))
                int out = move.checkIfMoveValid(selectedIds, numberToSelect);

                // Check if m is applicable and if not remove it from LM
                if (out == 1) { // valid
                    // if move m has been found then x := m(x) (accept m(x)):
                    // modify solution
                    selectedIds = move.modifySolution(selectedIds); 
                    movesToRemove.add(move); // remove the applied move
                    if (move.getType().equals("intra")){
                        // intra: affected edges are all between start & end
                        int indexIdj1 = selectedIds.indexOf(move.getNode1());
                        int indexIdj2 = selectedIds.indexOf(move.getNode2());
                        newlyAffectedNodes.addAll(selectedIds.subList(indexIdj2, indexIdj1 + 1));
                    } else {
                        // inter: affected only 2 edges: prev-> new node & new node -> next
                        newlyAffectedNodes.add(move.getNode2());
                        newlyAffectedNodes.add(move.getPrevNode());
                        newlyAffectedNodes.add(move.getNextNode());
                    }
                    go = true;
                    break;
                } else if (out == 0) {
                    movesToRemove.add(move);  // remove invalid moves
                } // else skip (out=2)
            }
            LM.removeAll(movesToRemove);
            affectedNodes = newlyAffectedNodes;
    }
    // until no move has been found after checking the whole list LM
    return selectedIds;
    }
}
