package test3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Meta-level Self-Organizing Map that organizes base SOMs
public class MetaSOM {
    private final int width;
    private final int height;
    private final int representationDimension;
    private final double[][][] weights;
    private final Map<BaseSOM, int[]> baseSOMPositions;
    private final List<MetaWarpTrail> metaWarpTrails;
    private final DistanceMetric metaMetric;
    private final Random random;
    
    public MetaSOM(int width, int height, int representationDimension) {
        this.width = width;
        this.height = height;
        this.representationDimension = representationDimension;
        this.weights = new double[width][height][representationDimension];
        this.baseSOMPositions = new ConcurrentHashMap<>();
        this.metaWarpTrails = new ArrayList<>();
        this.metaMetric = new EuclideanDistance(); // Meta-level uses Euclidean
        this.random = new Random();
        
        initializeWeights();
    }
    
    private void initializeWeights() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < representationDimension; k++) {
                    weights[i][j][k] = random.nextGaussian() * 0.5;
                }
            }
        }
    }
    
    private BMUResult findBMU(double[] representation) {
        double minDistance = Double.MAX_VALUE;
        int[] bmuPosition = new int[2];
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double distance = metaMetric.calculate(representation, weights[i][j]);
                if (distance < minDistance) {
                    minDistance = distance;
                    bmuPosition[0] = i;
                    bmuPosition[1] = j;
                }
            }
        }
        
        return new BMUResult(bmuPosition, minDistance);
    }
    
    public BMUResult organizeBaseSOM(BaseSOM baseSOM, double learningRate) {
        double[] representation = baseSOM.getRepresentation();
        BMUResult bmu = findBMU(representation);
        
        // Update meta-level weights
        double radius = Math.max(width, height) * 0.3;
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double distance = Math.sqrt(
                    Math.pow(i - bmu.position[0], 2) + 
                    Math.pow(j - bmu.position[1], 2)
                );
                
                if (distance <= radius) {
                    double influence = Math.exp(-distance * distance / (2 * radius * radius));
                    double effectiveLearningRate = learningRate * influence;
                    
                    for (int k = 0; k < representationDimension; k++) {
                        weights[i][j][k] += effectiveLearningRate * 
                            (representation[k] - weights[i][j][k]);
                    }
                }
            }
        }
        
        // Store position for visualization
        baseSOMPositions.put(baseSOM, Arrays.copyOf(bmu.position, bmu.position.length));
        
        // Create meta warp trail
        MetaWarpTrail metaTrail = new MetaWarpTrail(baseSOM, representation, bmu.position);
        metaWarpTrails.add(metaTrail);
        
        // Keep last 50 meta trails
        if (metaWarpTrails.size() > 500) {
            metaWarpTrails.remove(0);
        }
        
        return bmu;
    }
    
    public double[] getMetaRepresentation() {
        // Create representation of the meta-SOM state
        double sum = 0.0;
        double sumSquares = 0.0;
        int totalWeights = width * height * representationDimension;
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < representationDimension; k++) {
                    double weight = weights[i][j][k];
                    sum += weight;
                    sumSquares += weight * weight;
                }
            }
        }
        
        double mean = sum / totalWeights;
        double variance = (sumSquares / totalWeights) - (mean * mean);
        double organizationComplexity = baseSOMPositions.size() / 10.0; // Normalized
        double trailDensity = metaWarpTrails.size() / 50.0;
        
        return new double[]{mean, variance, organizationComplexity, trailDensity};
    }
    
    // Getters for visualization
    public Map<BaseSOM, int[]> getBaseSOMPositions() {
        return new HashMap<>(baseSOMPositions);
    }
    
    public List<MetaWarpTrail> getMetaWarpTrails() {
        return new ArrayList<>(metaWarpTrails);
    }
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getRepresentationDimension() { return representationDimension; }
    
    // Method to access weights for visualization
    public double[][][] getWeights() {
        double[][][] weightsCopy = new double[width][height][representationDimension];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                weightsCopy[i][j] = Arrays.copyOf(weights[i][j], representationDimension);
            }
        }
        return weightsCopy;
    }
    
    // Reset method for system reset
    public void reset() {
        baseSOMPositions.clear();
        metaWarpTrails.clear();
        initializeWeights();
    }
}