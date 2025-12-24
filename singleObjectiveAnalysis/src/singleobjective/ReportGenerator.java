package singleobjective;

import java.io.*;
import java.util.*;

/**
 * Generates CSV reports for single-objective analysis results.
 */
public class ReportGenerator {

    private DataParser dataParser;
    private String outputDir;

    public ReportGenerator(DataParser dataParser, String outputDir) {
        this.dataParser = dataParser;
        this.outputDir = outputDir;

        // Create output directory if it doesn't exist
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Generate all reports.
     */
    public void generateAllReports() throws IOException {
        System.out.println("\n=== Generating CSV Reports ===");
        System.out.println("Output directory: " + outputDir);

        generateSolutionCountReport();
        generateAveragePointsReport();
        generateAllSolutionsReport();

        System.out.println("\nAll reports generated successfully!");
    }

    /**
     * Generate report showing solution counts per algorithm, task count, and seed.
     * Report (a): Number of solutions for each algorithm with 10 different seed values.
     */
    public void generateSolutionCountReport() throws IOException {
        String fileName = outputDir + "/solution_counts.csv";
        System.out.println("Generating: " + fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Header
            StringBuilder header = new StringBuilder("Algorithm,TaskCount");
            for (int seed : dataParser.getSeeds()) {
                header.append(",Seed_").append(seed);
            }
            header.append(",Total");
            writer.println(header.toString());

            // Data rows
            for (String algoName : dataParser.getTargetAlgorithms()) {
                AlgorithmData data = dataParser.getAlgorithmData(algoName);

                for (int taskCount : dataParser.getTaskCounts()) {
                    StringBuilder row = new StringBuilder();
                    row.append(algoName).append(",").append(taskCount);

                    int total = 0;
                    for (int seed : dataParser.getSeeds()) {
                        int count = data.getSolutionCount(taskCount, seed);
                        row.append(",").append(count);
                        total += count;
                    }
                    row.append(",").append(total);

                    writer.println(row.toString());
                }
            }
        }
    }

    /**
     * Generate report with average points (X, Y, Z) for each algorithm per task count.
     * Report (c): Average values for every objective as X, Y, Z coordinates.
     */
    public void generateAveragePointsReport() throws IOException {
        String fileName = outputDir + "/average_points.csv";
        System.out.println("Generating: " + fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Header
            writer.println("Algorithm,AlgorithmType,Color,TaskCount,AvgMakespan_X,AvgEnergy_Y,AvgWaitTime_Z,SolutionCount,Label");

            // Data rows
            for (String algoName : dataParser.getTargetAlgorithms()) {
                AlgorithmData data = dataParser.getAlgorithmData(algoName);

                for (int taskCount : dataParser.getTaskCounts()) {
                    AveragePoint avg = data.getAveragePoint(taskCount);
                    if (avg == null) continue;

                    StringBuilder row = new StringBuilder();
                    row.append(algoName).append(",");
                    row.append(data.getType()).append(",");
                    row.append(data.getColorName()).append(",");
                    row.append(taskCount).append(",");
                    row.append(String.format("%.6f", avg.getAvgMakespan())).append(",");
                    row.append(String.format("%.6f", avg.getAvgEnergy())).append(",");
                    row.append(String.format("%.6f", avg.getAvgWaitTime())).append(",");
                    row.append(avg.getSolutionCount()).append(",");
                    row.append("\"").append(avg.getLabel()).append("\"");

                    writer.println(row.toString());
                }
            }
        }
    }

    /**
     * Generate report with all individual solutions.
     * Report (b): All objective values for every solution for every algorithm.
     */
    public void generateAllSolutionsReport() throws IOException {
        String fileName = outputDir + "/all_solutions.csv";
        System.out.println("Generating: " + fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Header
            writer.println("Algorithm,AlgorithmType,TaskCount,Seed,Makespan,Energy,AvgWaitTime");

            // Data rows
            for (String algoName : dataParser.getTargetAlgorithms()) {
                AlgorithmData data = dataParser.getAlgorithmData(algoName);

                for (int taskCount : dataParser.getTaskCounts()) {
                    for (int seed : dataParser.getSeeds()) {
                        List<Solution> solutions = data.getSolutions(taskCount, seed);
                        for (Solution sol : solutions) {
                            StringBuilder row = new StringBuilder();
                            row.append(algoName).append(",");
                            row.append(data.getType()).append(",");
                            row.append(taskCount).append(",");
                            row.append(seed).append(",");
                            row.append(String.format("%.6f", sol.getMakespan())).append(",");
                            row.append(String.format("%.6f", sol.getEnergy())).append(",");
                            row.append(String.format("%.6f", sol.getAvgWaitTime()));

                            writer.println(row.toString());
                        }
                    }
                }
            }
        }
    }

    /**
     * Generate JSON data file for Python plotting.
     *
     * @param objective1 First objective (X-axis)
     * @param objective2 Second objective (Y-axis)
     * @param taskCountFilter Optional filter for specific task counts
     * @param xMode If true, only include algorithms that optimize one of the selected objectives
     */
    public String generatePlotDataJson(String objective1, String objective2, int[] taskCountFilter, boolean xMode) throws IOException {
        String fileName = outputDir + "/plot_data_" + objective1 + "_vs_" + objective2 + ".json";
        System.out.println("Generating plot data: " + fileName);
        if (xMode) {
            System.out.println("  Xmode enabled: filtering algorithms by objectives " + objective1 + " and " + objective2);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("{");

            // Metadata
            writer.println("  \"objective1\": \"" + getObjectiveDisplayName(objective1) + "\",");
            writer.println("  \"objective2\": \"" + getObjectiveDisplayName(objective2) + "\",");
            writer.println("  \"xmode\": " + xMode + ",");

            // Task count filter info
            writer.print("  \"task_counts\": [");
            int[] tasksToUse = (taskCountFilter != null && taskCountFilter.length > 0) ?
                    taskCountFilter : dataParser.getTaskCounts();
            for (int i = 0; i < tasksToUse.length; i++) {
                writer.print(tasksToUse[i]);
                if (i < tasksToUse.length - 1) writer.print(", ");
            }
            writer.println("],");

            // Get algorithms to include (filtered if xMode is enabled)
            String[] allAlgorithms = dataParser.getTargetAlgorithms();
            List<String> algorithmsToInclude = new ArrayList<>();

            for (String algoName : allAlgorithms) {
                if (xMode) {
                    // Only include algorithms that optimize one of the selected objectives
                    String optimizedObjective = getOptimizedObjective(algoName);
                    if (optimizedObjective.equals(objective1) || optimizedObjective.equals(objective2)) {
                        algorithmsToInclude.add(algoName);
                    }
                } else {
                    algorithmsToInclude.add(algoName);
                }
            }

            // Algorithm data
            writer.println("  \"algorithms\": {");

            for (int a = 0; a < algorithmsToInclude.size(); a++) {
                String algoName = algorithmsToInclude.get(a);
                AlgorithmData data = dataParser.getAlgorithmData(algoName);

                writer.println("    \"" + algoName + "\": {");
                writer.println("      \"type\": \"" + data.getType() + "\",");
                writer.println("      \"color\": \"" + data.getColor() + "\",");
                writer.println("      \"points\": [");

                List<AveragePoint> points = new ArrayList<>();
                for (int tc : tasksToUse) {
                    AveragePoint avg = data.getAveragePoint(tc);
                    if (avg != null) {
                        points.add(avg);
                    }
                }

                for (int i = 0; i < points.size(); i++) {
                    AveragePoint avg = points.get(i);
                    double x = avg.getObjective(objective1);
                    double y = avg.getObjective(objective2);
                    String label = avg.getLabel();

                    writer.print("        {\"x\": " + x + ", \"y\": " + y +
                            ", \"label\": \"" + label + "\", \"task_count\": " + avg.getTaskCount() + "}");
                    if (i < points.size() - 1) writer.print(",");
                    writer.println();
                }

                writer.println("      ]");
                writer.print("    }");
                if (a < algorithmsToInclude.size() - 1) writer.print(",");
                writer.println();
            }

            writer.println("  }");
            writer.println("}");
        }

        return fileName;
    }

    /**
     * Determine which objective an algorithm optimizes based on its name.
     *
     * @param algoName Algorithm name (e.g., "GA_Energy", "SA_Makespan", "GA_ISL_AvgWait")
     * @return The objective it optimizes: "Makespan", "Energy", or "AvgWait"
     */
    private String getOptimizedObjective(String algoName) {
        String lowerName = algoName.toLowerCase();
        if (lowerName.contains("avgwait") || lowerName.contains("stt")) {
            return "AvgWait";
        } else if (lowerName.contains("energy") || lowerName.contains("power")) {
            return "Energy";
        } else if (lowerName.contains("makespan")) {
            return "Makespan";
        }
        return "Unknown";
    }

    /**
     * Generate JSON data file for 3D plotting.
     */
    public String generate3DPlotDataJson(int[] taskCountFilter) throws IOException {
        String fileName = outputDir + "/plot_data_3d.json";
        System.out.println("Generating 3D plot data: " + fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("{");

            // Metadata
            writer.println("  \"objective_x\": \"Makespan (s)\",");
            writer.println("  \"objective_y\": \"Energy Consumption (Wh)\",");
            writer.println("  \"objective_z\": \"Avg. Wait Time (s)\",");

            // Task count filter info
            writer.print("  \"task_counts\": [");
            int[] tasksToUse = (taskCountFilter != null && taskCountFilter.length > 0) ?
                    taskCountFilter : dataParser.getTaskCounts();
            for (int i = 0; i < tasksToUse.length; i++) {
                writer.print(tasksToUse[i]);
                if (i < tasksToUse.length - 1) writer.print(", ");
            }
            writer.println("],");

            // Algorithm data
            writer.println("  \"algorithms\": {");

            String[] algorithms = dataParser.getTargetAlgorithms();
            for (int a = 0; a < algorithms.length; a++) {
                String algoName = algorithms[a];
                AlgorithmData data = dataParser.getAlgorithmData(algoName);

                writer.println("    \"" + algoName + "\": {");
                writer.println("      \"type\": \"" + data.getType() + "\",");
                writer.println("      \"color\": \"" + data.getColor() + "\",");
                writer.println("      \"points\": [");

                List<AveragePoint> points = new ArrayList<>();
                for (int tc : tasksToUse) {
                    AveragePoint avg = data.getAveragePoint(tc);
                    if (avg != null) {
                        points.add(avg);
                    }
                }

                for (int i = 0; i < points.size(); i++) {
                    AveragePoint avg = points.get(i);
                    writer.print("        {\"x\": " + avg.getAvgMakespan() +
                            ", \"y\": " + avg.getAvgEnergy() +
                            ", \"z\": " + avg.getAvgWaitTime() +
                            ", \"label\": \"" + avg.getLabel() +
                            "\", \"task_count\": " + avg.getTaskCount() + "}");
                    if (i < points.size() - 1) writer.print(",");
                    writer.println();
                }

                writer.println("      ]");
                writer.print("    }");
                if (a < algorithms.length - 1) writer.print(",");
                writer.println();
            }

            writer.println("  }");
            writer.println("}");
        }

        return fileName;
    }

    private String getObjectiveDisplayName(String objective) {
        switch (objective) {
            case "Makespan": return "Makespan (s)";
            case "Energy": return "Energy Consumption (Wh)";
            case "AvgWait": return "Avg. Wait Time (s)";
            default: return objective;
        }
    }
}
