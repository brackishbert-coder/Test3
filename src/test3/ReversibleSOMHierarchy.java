package test3;

import java.util.*;

//Main system class that coordinates the entire hierarchy
public class ReversibleSOMHierarchy {
 private final List<BaseSOM> baseSOMList;
 private final MetaSOM metaSOM;
 private final List<DistanceMetric> metrics;
 private final Random random;
 private double learningRate;
 private boolean continuousLearning;
 
 // Configuration constants
 public static final int BASE_SOM_SIZE = 16;
 public static final int INPUT_DIMENSION = 256;
 public static final int META_SOM_SIZE = 8;
 public static final int REPRESENTATION_DIMENSION = 4;
 
 
 public ReversibleSOMHierarchy() {
     this.baseSOMList = new ArrayList<>();
     this.metaSOM = new MetaSOM(META_SOM_SIZE, META_SOM_SIZE, REPRESENTATION_DIMENSION);
     this.metrics = createDistanceMetrics();
     this.random = new Random();
     this.learningRate = 0.1;
     this.continuousLearning = true;
     
     initializeBaseSOM();
 }
 
 private List<DistanceMetric> createDistanceMetrics() {
     List<DistanceMetric> metrics = new ArrayList<>();
     metrics.add(new EuclideanDistance());
     metrics.add(new ManhattanDistance());
     metrics.add(new CosineDistance());
     metrics.add(new ChebyshevDistance());
     metrics.add(new MinkowskiDistance(3.0));
     return metrics;
 }
 
 private void initializeBaseSOM() {
     baseSOMList.clear();
     for (DistanceMetric metric : metrics) {
         BaseSOM som = new BaseSOM(BASE_SOM_SIZE, BASE_SOM_SIZE, INPUT_DIMENSION, metric);
         baseSOMList.add(som);
     }
 }
 
 public void processInput(double[] input) {
     if (!continuousLearning) return;
     
     // Update all base SOMs
     for (BaseSOM som : baseSOMList) {
         som.update(input, learningRate);
     }
     
     // Update meta-SOM with each base SOM's representation
     for (BaseSOM som : baseSOMList) {
         metaSOM.organizeBaseSOM(som, learningRate);
     }
 }
 
 
 

 public double[] generateRandomInput() {
     double[] input = new double[INPUT_DIMENSION];
     for (int i = 0; i < INPUT_DIMENSION; i++) {
         input[i] = random.nextGaussian();
     }
     return input;
 }
 
 public void runContinuousLearning(int iterations) {
     System.out.println("Starting continuous learning for " + iterations + " iterations...");
     
     for (int i = 0; i < iterations; i++) {
         double[] input = generateRandomInput();
         processInput(input);
         
         if (i % 100 == 0) {
             printStatus(i);
         }
     }
     
     System.out.println("Continuous learning completed.");
 }
 
 private void printStatus(int iteration) {
     System.out.printf("Iteration %d - Active SOMs: %d, Meta trails: %d%n", 
         iteration, baseSOMList.size(), metaSOM.getMetaWarpTrails().size());
     
     // Print SOM positions in meta-space
     Map<BaseSOM, int[]> positions = metaSOM.getBaseSOMPositions();
     for (int i = 0; i < baseSOMList.size(); i++) {
         BaseSOM som = baseSOMList.get(i);
         int[] pos = positions.get(som);
         if (pos != null) {
             System.out.printf("  %s SOM -> Meta position: [%d, %d], Trails: %d%n",
                 som.getMetricName(), pos[0], pos[1], som.getWarpTrailCount());
         }
     }
     System.out.println();
 }
 
 public void demonstrateReversibility() {
     System.out.println("Demonstrating reversibility...");
     
     // Generate test input
     double[] originalInput = generateRandomInput();
     System.out.println("Original input: " + Arrays.toString(originalInput));
     
     // Process through hierarchy
     processInput(originalInput);
     
     // Try to reconstruct from each base SOM
     for (BaseSOM som : baseSOMList) {
         double[] representation = som.getRepresentation();
         double[] reconstructed = som.reconstruct(representation);
         
         if (reconstructed != null) {
             double error = new EuclideanDistance().calculate(originalInput, reconstructed);
             System.out.printf("%s SOM reconstruction error: %.6f%n", 
                 som.getMetricName(), error);
         }
     }
     System.out.println();
 }
 
 public void visualizeMetaOrganization() {
     System.out.println("Meta-SOM Organization:");
     System.out.println("=====================");
     
     Map<BaseSOM, int[]> positions = metaSOM.getBaseSOMPositions();
     
     // Create a grid visualization
     String[][] grid = new String[META_SOM_SIZE][META_SOM_SIZE];
     for (int i = 0; i < META_SOM_SIZE; i++) {
         for (int j = 0; j < META_SOM_SIZE; j++) {
             grid[i][j] = "  .  ";
         }
     }
     
     // Place SOMs on grid
     for (int i = 0; i < baseSOMList.size(); i++) {
         BaseSOM som = baseSOMList.get(i);
         int[] pos = positions.get(som);
         if (pos != null && pos[0] < META_SOM_SIZE && pos[1] < META_SOM_SIZE) {
             String abbrev = som.getMetricName().substring(0, Math.min(3, som.getMetricName().length()));
             grid[pos[1]][pos[0]] = String.format(" %3s ", abbrev);
         }
     }
     
     // Print grid
     for (int i = 0; i < META_SOM_SIZE; i++) {
         for (int j = 0; j < META_SOM_SIZE; j++) {
             System.out.print(grid[i][j]);
         }
         System.out.println();
     }
     System.out.println();
 }
 
 public void reset() {
     initializeBaseSOM();
     metaSOM.reset();
 }
 
 // Getters and setters
 public void setLearningRate(double learningRate) {
     this.learningRate = learningRate;
 }
 
 public double getLearningRate() {
     return learningRate;
 }
 
 public void setContinuousLearning(boolean continuousLearning) {
     this.continuousLearning = continuousLearning;
 }
 
 public boolean isContinuousLearning() {
     return continuousLearning;
 }
 
 public List<BaseSOM> getBaseSOMList() {
     return new ArrayList<>(baseSOMList);
 }
 
 public MetaSOM getMetaSOM() {
     return metaSOM;
 }
 
 public List<DistanceMetric> getMetrics() {
     return new ArrayList<>(metrics);
 }
 
 // Main method for standalone testing
 public static void main(String[] args) {
     System.out.println("Reversible Multi-Metric SOM Hierarchy");
     System.out.println("=====================================");
     
     ReversibleSOMHierarchy hierarchy = new ReversibleSOMHierarchy();
     
     // Run continuous learning
     hierarchy.runContinuousLearning(1000);
     
     // Visualize organization
     hierarchy.visualizeMetaOrganization();
     
     // Demonstrate reversibility
     hierarchy.demonstrateReversibility();
     
     // Show final status
     System.out.println("Final System State:");
     hierarchy.printStatus(1000);
 }
}