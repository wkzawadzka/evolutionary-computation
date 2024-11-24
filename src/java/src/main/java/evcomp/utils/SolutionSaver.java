package evcomp.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SolutionSaver {
    
    private String weekName;
    private String methodName;
    private String instance;

    public SolutionSaver(String weekName, String methodName, String instance) {
        this.weekName = weekName;
        this.methodName = methodName;
        this.instance = instance;
    }

    /**
     * Saves the solution details to a file.
     *
     * @param solution List of node IDs selected in this run.
     * @param runNumber The number of this run (1, 2, ...).
     * @param timeTaken Time taken for the method execution.
     * @param totalCost Total cost calculated.
     * @param totalDistance Total distance calculated.
     * @param objFuncValue Value of the objective function.
     */
    public void saveSolution(List<Integer> solution, int runNumber, long timeTaken, 
                             int totalCost, int totalDistance, int objFuncValue) {
        Path directoryPath = Paths.get("data", "method_outputs", weekName, methodName, instance);
        File dir = directoryPath.toFile();

        // Create directory if it does not exist
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = directoryPath.resolve(runNumber + ".txt").toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write the metadata at the top of the file
            writer.write(String.valueOf(timeTaken)); //nanos
            writer.newLine();
            writer.write(String.valueOf(totalCost));
            writer.newLine();
            writer.write(String.valueOf(totalDistance));
            writer.newLine();
            writer.write(String.valueOf(objFuncValue));
            writer.newLine();
            writer.write("Solution:");
            writer.newLine();

            // Write the solution
            for (Integer id : solution) {
                writer.write(id.toString());
                writer.newLine();
            }
            System.out.println("Solution saved to: " + filePath);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
    public void saveSolution(List<Integer> solution, int runNumber, long timeTaken,
                             int totalCost, int totalDistance, int objFuncValue, int count) {
        Path directoryPath = Paths.get("data", "method_outputs", weekName, methodName, instance);
        File dir = directoryPath.toFile();

        // Create directory if it does not exist
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = directoryPath.resolve(runNumber + ".txt").toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write the metadata at the top of the file
            writer.write(String.valueOf(count)); //nanos
            writer.newLine();
            writer.write(String.valueOf(timeTaken)); //nanos
            writer.newLine();
            writer.write(String.valueOf(totalCost));
            writer.newLine();
            writer.write(String.valueOf(totalDistance));
            writer.newLine();
            writer.write(String.valueOf(objFuncValue));
            writer.newLine();
            writer.write("Solution:");
            writer.newLine();

            // Write the solution
            for (Integer id : solution) {
                writer.write(id.toString());
                writer.newLine();
            }
            System.out.println("Solution saved to: " + filePath);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}
