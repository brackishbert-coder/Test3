package test3;
import java.util.*;

//Individual Self-Organizing Map with specific distance metric
public class BaseSOM {
 private final int width;
 private final int height;
 private final int inputDimension;
 private final DistanceMetric metric;
 private final String metricName;
 private final double[][][] weights;
 private final List<WarpTrailNode> warpTrails;
 private final Random random;
 
 public BaseSOM(int width, int height, int inputDimension, DistanceMetric metric) {
     this.width = width;
     this.height = height;
     this.inputDimension = inputDimension;
     this.metric = metric;
     this.metricName = metric.getName();
     this.weights = new double[width][height][inputDimension];
     this.warpTrails = new ArrayList<>();
     this.random = new Random();
     
     initializeWeights();
 }
 
 private void initializeWeights() {
     for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
             for (int k = 0; k < inputDimension; k++) {
                 weights[i][j][k] = random.nextGaussian() * 0.5;
             }
         }
     }
 }
 
 public BMUResult findBMU(double[] input) {
     double minDistance = Double.MAX_VALUE;
     int[] bmuPosition = new int[2];
     
     for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
             double distance = metric.calculate(input, weights[i][j]);
             if (distance < minDistance) {
                 minDistance = distance;
                 bmuPosition[0] = i;
                 bmuPosition[1] = j;
             }
         }
     }
     
     return new BMUResult(bmuPosition, minDistance);
 }
 
 public BMUResult update(double[] input, double learningRate) {
     BMUResult bmu = findBMU(input);
     WarpTrailNode trail = new WarpTrailNode(input, bmu.position);
     
     // Calculate neighborhood radius (decreases over time)
     double maxDimension = Math.max(width, height);
     double radius = maxDimension * 0.5 * Math.exp(-warpTrails.size() / 1000.0);
     
     // Update weights in neighborhood
     for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
             double distance = Math.sqrt(
                 Math.pow(i - bmu.position[0], 2) + 
                 Math.pow(j - bmu.position[1], 2)
             );
             
             if (distance <= radius) {
                 double influence = Math.exp(-distance * distance / (2 * radius * radius));
                 double effectiveLearningRate = learningRate * influence;
                 
                 double[] oldWeights = Arrays.copyOf(weights[i][j], inputDimension);
                 
                 // Update weights
                 for (int k = 0; k < inputDimension; k++) {
                     weights[i][j][k] += effectiveLearningRate * (input[k] - weights[i][j][k]);
                 }
                 
                 // Record transformation for warp trail
                 trail.transformations.add(new WeightTransformation(
                     new int[]{i, j}, oldWeights, 
                     Arrays.copyOf(weights[i][j], inputDimension), influence
                 ));
             }
         }
     }
     
     // Add to warp trails (keep last 100)
     warpTrails.add(trail);
     if (warpTrails.size() > 100) {
         warpTrails.remove(0);
     }
     
     return bmu;
 }
 
 public double[] getRepresentation() {
     // Create compact representation of SOM state
     double sum = 0.0;
     double sumSquares = 0.0;
     int totalWeights = width * height * inputDimension;
     
     for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
             for (int k = 0; k < inputDimension; k++) {
                 double weight = weights[i][j][k];
                 sum += weight;
                 sumSquares += weight * weight;
             }
         }
     }
     
     double mean = sum / totalWeights;
     double variance = (sumSquares / totalWeights) - (mean * mean);
     double entropy = calculateEntropy();
     double trailDensity = warpTrails.size() / 100.0;
     
     return new double[]{mean, variance, entropy, trailDensity};
 }
 
 private double calculateEntropy() {
     Map<Integer, Integer> histogram = new HashMap<>();
     double binSize = 0.1;
     
     for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
             for (int k = 0; k < inputDimension; k++) {
                 int bin = (int) Math.floor(weights[i][j][k] / binSize);
                 histogram.put(bin, histogram.getOrDefault(bin, 0) + 1);
             }
         }
     }
     
     int totalBins = width * height * inputDimension;
     double entropy = 0.0;
     
     for (int count : histogram.values()) {
         double probability = (double) count / totalBins;
         if (probability > 0) {
             entropy -= probability * Math.log(probability) / Math.log(2);
         }
     }
     
     return entropy;
 }
 
 public double[] reconstruct(double[] representation) {
     // Simplified reconstruction using latest warp trail
     if (warpTrails.isEmpty()) {
         return null;
     }
     
     WarpTrailNode latestTrail = warpTrails.get(warpTrails.size() - 1);
     return Arrays.copyOf(latestTrail.input, latestTrail.input.length);
 }
 
 // Public getters for visualization
 public String getMetricName() { return metricName; }
 public int getWarpTrailCount() { return warpTrails.size(); }
 public List<WarpTrailNode> getWarpTrails() { return new ArrayList<>(warpTrails); }
 public int getWidth() { return width; }
 public int getHeight() { return height; }
 public int getInputDimension() { return inputDimension; }
 
 // Method to access weights for visualization (package-private)
 public double[][][] getWeights() {
     // Return a copy to maintain encapsulation
     double[][][] weightsCopy = new double[width][height][inputDimension];
     for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
             weightsCopy[i][j] = Arrays.copyOf(weights[i][j], inputDimension);
         }
     }
     return weightsCopy;
 }
}