package evcomp.w7_large_neigh_search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap; // For SimpleEntry (to represent tuple-like structure)
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import evcomp.utils.Evaluator;
import evcomp.utils.InputGenerator;
import evcomp.utils.Move;
import evcomp.utils.Node;
import evcomp.utils.SolutionSaver;


public class LargeNeighborhoodSearch {
    private static Random RANDOM;
    private static Evaluator evaluator;
    private static double weight = 0.5;
    private static int n_iterations;

    public static void main(String[] args) {
        // Args
        if (args.length != 2) {
            System.out.println("Usage: java IteratedLocalSearch <instance> <n_experiments>");
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

        SolutionSaver solutionSaver = new SolutionSaver("w7_large_neigh_search", "LargeNeighborhoodSearch", instance);
        // Perform experiment - WO local search
        for (int i = 1; i <= nExperiments; i++) {
            n_iterations = 0;
            List<Integer> initialSolution = generateInitialRandomSolution(ids, nodeList, distanceMatrix);
            // *************************************
            long startTime = System.currentTimeMillis();

            List<Integer> solution = generateLNS(initialSolution, nodeList, distanceMatrix, startTime, false, 13383);

            long timeTaken = System.currentTimeMillis() - startTime;
            // *************************************
            int totalCost = evaluator.calculateTotalCost(solution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(solution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            System.out.println(objFuncValue);
            solutionSaver.saveSolution(solution, i, timeTaken, totalCost, totalDistance, objFuncValue, n_iterations);
        }

        SolutionSaver solutionSaver2 = new SolutionSaver("w7_large_neigh_search", "LargeNeighborhoodSearchLS", instance);
        // Perform experiment - WITH local search
        for (int i = 1; i <= nExperiments; i++) {
            n_iterations = 0;
            List<Integer> initialSolution = generateInitialRandomSolution(ids, nodeList, distanceMatrix);
            // *************************************
            long startTime = System.currentTimeMillis();

            List<Integer> solution = generateLNS(initialSolution, nodeList, distanceMatrix, startTime, true, 13383);

            long timeTaken = System.currentTimeMillis() - startTime;
            // *************************************
            int totalCost = evaluator.calculateTotalCost(solution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(solution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            System.out.println(objFuncValue);
            solutionSaver2.saveSolution(solution, i, timeTaken, totalCost, totalDistance, objFuncValue, n_iterations);
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

    private static int evaluate(List<Integer> solution, List<Node> nodeList, int[][] distanceMatrix){
        int totalCost = evaluator.calculateTotalCost(solution, nodeList);
        int totalDistance = evaluator.calculateTotalDistance(solution, distanceMatrix);
        int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);
        return(objFuncValue);
    }

    private static List<Integer> destroy(List<Integer> solution, List<Node> nodeList, int[][] distanceMatrix) {
        List<Integer> destroyed = new ArrayList<>(solution);

        int n_subpaths = RANDOM.nextInt(2) + 2; // randomly 2 or 3 subpaths
        int percentage = RANDOM.nextInt(11) + 20; // randomly 20-30% nodes
        //System.out.println("n_subpaths = " + n_subpaths + " and percentage: " + percentage);
        int length_subpath = (solution.size() * percentage) / 100 / n_subpaths;
        int n_to_remove = solution.size() - ((solution.size() * percentage) / 100);

        List<Map.Entry<List<Integer>, Integer>> subpaths = new ArrayList<>();
        for (int i = 0; i < solution.size() - length_subpath + 1; i++) {
            List<Integer> subpath = solution.subList(i, i + length_subpath);
            Integer obj_f = evaluate(subpath, nodeList, distanceMatrix);
            subpaths.add(new AbstractMap.SimpleEntry<>(new ArrayList<>(subpath), obj_f));
        }

        // sort subpaths by objective function in descending order (highest scores first)
        subpaths.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // remove
        for (int i = 0; i < n_subpaths && i < subpaths.size(); i++) {
            List<Integer> toRemove = subpaths.get(i).getKey(); // get the subpath
            destroyed.removeAll(toRemove); // remove all nodes in this subpath from the solution
        }
        int n_removed = solution.size() - destroyed.size();

        while (n_removed < n_to_remove) {
            int randomIndex = RANDOM.nextInt(destroyed.size()); // Random index
            destroyed.remove(randomIndex); // Remove a random node
            n_removed++;
        }

        return destroyed;
    }

    private static List<Integer> generateLNS(List<Integer> initialSolution, List<Node> nodeList, int[][] distanceMatrix, long startTime, boolean withLocalSearch, int maxTime) {
        // (1) Generate an initial solution x = initialSolution (As the starting solution use random solution.)
        // (2) x := Local search (x) (optional) - Always apply local search to the initial solution.
        List<Integer> x = localSearch(initialSolution, nodeList, distanceMatrix); // generateSteepest2edgesMoveEvals
        int bestF = evaluate(x, nodeList, distanceMatrix);
        // (3) Repeat until stopping conditions are met
        long startingTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startingTime < maxTime){
            // y := Destroy (x)
            // removed edges selected at random as several subpaths (2-3 subpaths) -> as there may be different regions of solutions
            // (. You can try to propose some heuristic rules to remove “bad” nodes/edges, e.g. long edges
            // or costly nodes. Such heuristics should be, however, randomized not completely deterministic. For
            // example the probability of removal should depend on the length/cost.)
            List<Integer> y = destroy(x, nodeList, distanceMatrix);

            // y := Repair (y)
            // As repair operator use the best greedy heuristic (including greedy-regret) from previous assignments.
            y = repair(y, nodeList, distanceMatrix);

            // y := Local search (y) (optional)
            if (withLocalSearch) {
                y = localSearch(y, nodeList, distanceMatrix);
            }

            // If f(y) > f(x) then
            //     x := y
            int currentCost = evaluate(y, nodeList, distanceMatrix);
            //System.out.println("[i=" + n_iterations +  "] currCost:  " + currentCost);
            if (currentCost < bestF) {
                x = new ArrayList<>(y);
                bestF = currentCost;
            }

            n_iterations++;
        }
        return x;
    }

    private static List<Integer> repair(List<Integer> partialSolution, List<Node> nodeList, int[][] distanceMatrix) {
        int totalNodes = nodeList.size();
        int numberToSelect = totalNodes / 2; // Select 50%
        // use a greedy heuristic to repair the solution
        // Greedy2RegretWeightedSum

        // track visited nodes
        boolean[] visited = new boolean[totalNodes];
        for (int node : partialSolution) {
            visited[node] = true; // Mark nodes in the partial solution as visited
        }

        // start from the given partial solution
        List<Integer> currCycle = new ArrayList<>(partialSolution);

        // Repeat until all nodes are added
        while (currCycle.size() < numberToSelect) {
            int bestNodeIndex = -1;
            int bestPosition = -1;
            double bestWeightedSum = Double.MAX_VALUE;

            // Iterate over all unvisited nodes
            for (int j = 0; j < totalNodes; j++) {
                if (!visited[j]) {
                    int newNode = j;
                    int bestInc = Integer.MAX_VALUE;
                    int secondInc = Integer.MAX_VALUE;
                    int bestPlace = -1;

                    // Find the best and second-best position for this node
                    for (int pos = 0; pos < currCycle.size(); pos++) {
                        int prevNode = currCycle.get(pos);
                        int nextNode = currCycle.get((pos + 1) % currCycle.size());

                        // Obj.func. change
                        int increase = distanceMatrix[prevNode][newNode] // A -> new
                                + distanceMatrix[newNode][nextNode] // new -> B
                                - distanceMatrix[prevNode][nextNode]
                                + nodeList.get(newNode).getCost(); // remove previous A -> B

                        if (increase < bestInc) {
                            secondInc = bestInc;
                            bestInc = increase;
                            bestPlace = pos + 1;
                        } else if (increase < secondInc) {
                            secondInc = increase;
                        }
                    }

                    // regret
                    int regret = secondInc - bestInc;

                    // weighted sum: objective function (greedy) + regret (weighted)
                    // bestInc is objective func val
                    double weightedSum = (1-weight) * bestInc + weight * (-1.0 * regret);
                    // Select the node with the smallest weighted sum
                    if (weightedSum < bestWeightedSum) {
                        bestNodeIndex = newNode;
                        bestPosition = bestPlace;
                        bestWeightedSum = weightedSum;
                    }
                }
            }

                // insert selected node into best position
                currCycle.add(bestPosition, bestNodeIndex);
                visited[bestNodeIndex] = true;
            }

        return currCycle;
    }
    


    // generateSteepest2edgesMoveEvals:
    private static List<Integer> localSearch(List<Integer> initialSolution, List<Node> nodeList, int[][] distanceMatrix) {
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
