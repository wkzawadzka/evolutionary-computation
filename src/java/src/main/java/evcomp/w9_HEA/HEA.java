package evcomp.w9_HEA;

import evcomp.utils.Evaluator;
import evcomp.utils.InputGenerator;
import evcomp.utils.Move;
import evcomp.utils.Node;
import evcomp.utils.SolutionSaver;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


public class HEA {
    private static Random RANDOM;
    private static Evaluator evaluator;
    private static double weight = 0.5;
    private static double mutationProbability = 0.35;
    private static boolean useMutation = true;

    // zmienne
    private static boolean doLSafterRecomb = false;
    private static Integer operator = 2;
    private static String name = "HEA_op2";

    public static void main(String[] args) {
        // Args
        if (args.length != 2) {
            System.out.println("Usage: java HEA <instance> <n_experiments>");
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


        SolutionSaver solutionSaver = new SolutionSaver("w9_HEA", name, instance);
        // Perform experiment
        for (int i = 1; i <= nExperiments; i++) {
            // *************************************
            // Initial population = 20, random; same as LNS so generateSteepest2edgesMoveEvals.
            List<List<Integer>> initialpop = new ArrayList<>();
            for (int id = 0; id < 20; id++) {
                List<Integer> sol = generateInitialRandomSolution(ids, nodeList, distanceMatrix); // generate a random solution
                initialpop.add(sol); // add the solution to the initial population
            }
            //System.out.println("Initial Population: " + initialpop);
            long startTime = System.currentTimeMillis();

            List<Integer> solution = generateHEA(initialpop, nodeList, distanceMatrix, startTime, operator, 13383);

            long timeTaken = System.currentTimeMillis() - startTime;
            // *************************************
            int totalCost = evaluator.calculateTotalCost(solution, nodeList);
            int totalDistance = evaluator.calculateTotalDistance(solution, distanceMatrix);
            int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);

            System.out.println(objFuncValue);
            solutionSaver.saveSolution(solution, i, timeTaken, totalCost, totalDistance, objFuncValue);
        }
    }
    
    

    private static List<Integer> generateHEA(List<List<Integer>> initialpop, List<Node> nodeList, int[][] distanceMatrix, long startTime, int operator, int maxTime) {
        List<Integer> costs = evaluatePopulation(initialpop, nodeList, distanceMatrix);
        List<Integer> child = new ArrayList<>();
        int childF;
        int idWorstCost;
        List<List<Integer>> pop = initialpop;

        // (3) Repeat until stopping conditions are met
        long startingTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startingTime < maxTime) {
            // System.out.println("CURRENT BEST: " + costs.get(getIdWithGivenCost("min", costs)));
            // select parents
            List<List<Integer>> parents = pickNRandom(pop, 2);

            // offspring creation
            if (operator == 1){
                child = operator1(parents, nodeList);
            } else {
                child = operator2(parents, nodeList, distanceMatrix);

            }

            if (doLSafterRecomb){
                child = localSearch(child, nodeList, distanceMatrix);
            }
            if (RANDOM.nextDouble() < mutationProbability && useMutation) {
                // Perform mutation: destroy and repair from LNS
                child = destroy(child, nodeList, distanceMatrix);
                child = repair(child, nodeList, distanceMatrix); 
            }

            childF = evaluate(child, nodeList, distanceMatrix);
            if (!costs.contains(childF)) {
                idWorstCost = getIdWithGivenCost("max", costs);
            
                // if the child's cost is better (lower), replace the worst solution
                if (costs.get(idWorstCost) > childF) {
                    pop.set(idWorstCost, child); 
                    costs.set(idWorstCost, childF); 
                } // elite 20 
            }

        }
        return pop.get(getIdWithGivenCost("min", costs)); // best solution is returned (lowest F)
    }

    public static List<Integer> operator2(List<List<Integer>> parents, List<Node> nodeList, int[][] distanceMatrix) {
        // Step 1: Get the parents
        List<Integer> parent1 = parents.get(0);
        List<Integer> parent2 = parents.get(1);
        int size = parent1.size();

        // Step 2: Start with one parent as the initial solution
        List<Integer> child = new ArrayList<>(parent1);

        // Step 3: Remove edges and nodes from the first parent that are not in the second parent
        // A B C D E F
        // A E C F D G
        // if edge DE not in second parent, but nodes D E in, then delete DE either way (?)
        // but we could leave then either C -> E or D -> F instead - which to choose
        // for i and i+1 (edge) in first parent, if not in second then
        //     if any of nodes i, i+1nnot in sol remove this
        //     else remove i
        // or just remove i each time
        // first - remove not common nodes
        Set<Integer> parent2Set = new HashSet<>(parent2);
        child.removeIf(node -> !parent2Set.contains(node));  
        int sizeChild = child.size();
        // second - remove edges not p2
        // so we have the same nodes, but now the order shall be preserved
        Set<Set<Integer>> edges1 = new HashSet<>();
        Set<Set<Integer>> edges2 = new HashSet<>();
        for (int i = 0; i < size; i++) {
            int next = (i + 1) % size;
            edges1.add(new HashSet<>(Arrays.asList(parent1.get(i), parent1.get(next))));
            edges2.add(new HashSet<>(Arrays.asList(parent2.get(i), parent2.get(next))));
        }
        Set<Set<Integer>> commonEdges = new HashSet<>(edges1);
        commonEdges.retainAll(edges2);
        List<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i < sizeChild - 1; i++) {
            Set<Integer> edge = new HashSet<>(Arrays.asList(child.get(i), child.get(i + 1)));
            if (!commonEdges.contains(edge)) {
                toRemove.add(i);
            }
        }
        child.removeAll(toRemove);
        // System.err.println(child.size());
    
        // Step 4: repair
        child = repair(child, nodeList, distanceMatrix);

        return child;
    }

    public static List<Integer> operator1(List<List<Integer>> parents, List<Node> nodeList) {
        // Step 1: Get the parents
        List<Integer> parent1 = parents.get(0);
        List<Integer> parent2 = parents.get(1);
        int size = parent1.size();
    
        // Step 2: Initialize the offspring with null values
        List<Integer> offspring = new ArrayList<>(Collections.nCopies(size, null));
    
        // Step 3: Locate common nodes and edges in both parents
        // A -> edges 
        Set<Set<Integer>> edges1 = new HashSet<>();
        Set<Set<Integer>> edges2 = new HashSet<>();
        for (int i = 0; i < size; i++) {
            int next = (i + 1) % size;
            edges1.add(new HashSet<>(Arrays.asList(parent1.get(i), parent1.get(next))));
            edges2.add(new HashSet<>(Arrays.asList(parent2.get(i), parent2.get(next))));
        }
        Set<Set<Integer>> commonEdges = new HashSet<>(edges1);
        commonEdges.retainAll(edges2);
    
        // place in offspring (in order of p1 simply)
        for (int i = 0; i < size - 1; i++) {
            Set<Integer> edge = new HashSet<>(Arrays.asList(parent1.get(i), parent1.get(i + 1)));
            if (commonEdges.contains(edge)) {
                offspring.set(i, parent1.get(i));
                offspring.set(i + 1, parent1.get(i + 1));
            }
        }

        // B -> nodes
        Set<Integer> commonNodes = new HashSet<>(parent1);
        commonNodes.retainAll(parent2); // intersection p1&p2
        commonNodes.removeAll(offspring); // - child (not already in solution)

        // place in offspring
        for (Integer node : commonNodes) {
            for (int i = 0; i < size; i++) {
                if (offspring.get(i) == null) {
                    offspring.set(i, node);
                    break;
                }
            }
        }

    
        // Step 4: Fill remaining null positions with random nodes, ensuring no duplicates
        List<Integer> remainingNodes = new ArrayList<>();
        for (Node node : nodeList) {
            int nodeId = node.getId();
            if (!offspring.contains(nodeId)) {
                remainingNodes.add(nodeId);
            }
        }
        Collections.shuffle(remainingNodes); // shuffle to randomize remaining nodes
        int remainingIndex = 0;
        for (int i = 0; i < size; i++) {
            if (offspring.get(i) == null) {
                offspring.set(i, remainingNodes.get(remainingIndex));
                remainingIndex++;
            }
        }
    
        return offspring;
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

        // calculate probabilities based on the highest score
        int highestScore = subpaths.get(0).getValue();
        List<Double> probabilities = new ArrayList<>();
        for (Map.Entry<List<Integer>, Integer> entry : subpaths) {
            probabilities.add(entry.getValue() / (double) highestScore); // normalize by highest score
        }

        // remove
        // Randomly select and remove subpaths based on probabilities
        for (int i = 0; i < n_subpaths && !subpaths.isEmpty(); i++) {
            double rand = RANDOM.nextDouble(); // generate random probability [0, 1)
            double cumulativeProbability = 0.0;
            for (int j = 0; j < subpaths.size(); j++) {
                cumulativeProbability += probabilities.get(j);
                if (rand <= cumulativeProbability) {
                    List<Integer> toRemove = subpaths.get(j).getKey(); 
                    destroyed.removeAll(toRemove); 
                    subpaths.remove(j); 
                    probabilities.remove(j); 
                    break;
                }
            }
        }
        int n_removed = solution.size() - destroyed.size();

        while (n_removed < n_to_remove) {
            int randomIndex = RANDOM.nextInt(destroyed.size()); // Random index
            destroyed.remove(randomIndex); // Remove a random node
            n_removed++;
        }

        return destroyed;
    }

    private static List<Integer> generateInitialRandomSolution(List<Integer> ids, List<Node> nodeList, int[][] distanceMatrix) {
        // (1) Generate an initial solution x = initialSolution (As the starting solution use random solution.)
        int totalNodes = nodeList.size();
        int numberToSelect = totalNodes / 2; // Select 50%

        Collections.shuffle(ids, RANDOM);
        List<Integer> randomSolution = new ArrayList<>();
        for (int j = 0; j < numberToSelect; j++) {
            randomSolution.add(ids.get(j));
        }

        // (2) x := Local search (x) (optional) - Always apply local search to the initial solution.
        List<Integer> x = localSearch(randomSolution, nodeList, distanceMatrix); // generateSteepest2edgesMoveEvals
        return x;
    }
    
    private static int evaluate(List<Integer> solution, List<Node> nodeList, int[][] distanceMatrix){
        int totalCost = evaluator.calculateTotalCost(solution, nodeList);
        int totalDistance = evaluator.calculateTotalDistance(solution, distanceMatrix);
        int objFuncValue = evaluator.calculateObjectiveFunction(totalCost, totalDistance);
        return(objFuncValue);
    }

    private static List<Integer> evaluatePopulation(List<List<Integer>> population, List<Node> nodeList, int[][] distanceMatrix) {
        List<Integer> costs = new ArrayList<>(); 

        // Iterate through each solution in the population
        for (List<Integer> solution : population) {
            int cost = evaluate(solution, nodeList, distanceMatrix); 
            costs.add(cost); 
        }

        return costs; // Return the list of costs
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

    public static List<List<Integer>> pickNRandom(List<List<Integer>> lst, int n) {
        List<List<Integer>> copy = new ArrayList<>(lst); // create a copy to avoid modifying the original list
        Collections.shuffle(copy); 
        return copy.subList(0, Math.min(n, copy.size())); 
    }


    private static Integer getIdWithGivenCost(String type, List<Integer> costs) {
        if (type.equals("max")) { 
            int maxCostIndex = 0;
            for (int i = 1; i < costs.size(); i++) {
                if (costs.get(i) > costs.get(maxCostIndex)) {
                    maxCostIndex = i;
                }
            }
            return maxCostIndex;
        } else {  // for "min"
            int minCostIndex = 0;
            for (int i = 1; i < costs.size(); i++) {
                if (costs.get(i) < costs.get(minCostIndex)) {
                    minCostIndex = i;
                }
            }
            return minCostIndex;
        }
    }
}