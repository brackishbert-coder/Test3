package test3;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Feature-based vectorizer with fixed 16×16 output.
 * Each of the 256 elements represents local brightness,
 * edge strength, and texture variance from a region of
 * the full webcam image.
 */
public class FeatureVectorizer {

    private final WebcamUpdater webcam;
    private static final int GRID_W = 16;
    private static final int GRID_H = 16;

    public FeatureVectorizer(WebcamUpdater webcam) {
        this.webcam = webcam;
    }

    public double[] getFeatureVector() {
        BufferedImage img = webcam.getImage();
        if (img == null) {
            return new double[GRID_W * GRID_H];
        }

        int srcW = img.getWidth();
        int srcH = img.getHeight();
        double cellW = srcW / (double) GRID_W;
        double cellH = srcH / (double) GRID_H;

        // Convert to grayscale buffer
        double[][] gray = new double[srcH][srcW];
        for (int y = 0; y < srcH; y++) {
            for (int x = 0; x < srcW; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                gray[y][x] = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
            }
        }

        // Compute Sobel edge map
        double[][] edge = new double[srcH][srcW];
        for (int y = 1; y < srcH - 1; y++) {
            for (int x = 1; x < srcW - 1; x++) {
                double gx = (gray[y - 1][x + 1] + 2 * gray[y][x + 1] + gray[y + 1][x + 1])
                          - (gray[y - 1][x - 1] + 2 * gray[y][x - 1] + gray[y + 1][x - 1]);
                double gy = (gray[y + 1][x - 1] + 2 * gray[y + 1][x] + gray[y + 1][x + 1])
                          - (gray[y - 1][x - 1] + 2 * gray[y - 1][x] + gray[y - 1][x + 1]);
                edge[y][x] = Math.sqrt(gx * gx + gy * gy);
            }
        }

        // Build feature map
        double[] vector = new double[GRID_W * GRID_H];
        double maxVal = 1e-9;

        for (int gy = 0; gy < GRID_H; gy++) {
            for (int gx = 0; gx < GRID_W; gx++) {
                int x0 = (int) Math.round(gx * cellW);
                int y0 = (int) Math.round(gy * cellH);
                int x1 = (int) Math.round((gx + 1) * cellW);
                int y1 = (int) Math.round((gy + 1) * cellH);
                x1 = Math.min(x1, srcW);
                y1 = Math.min(y1, srcH);

                double sumGray = 0, sumEdge = 0, sumSq = 0;
                int count = 0;
                for (int yy = y0; yy < y1; yy++) {
                    for (int xx = x0; xx < x1; xx++) {
                        double g = gray[yy][xx];
                        double e = edge[yy][xx];
                        sumGray += g;
                        sumEdge += e;
                        sumSq += g * g;
                        count++;
                    }
                }

                double meanGray = sumGray / count;
                double meanEdge = sumEdge / count;
                double variance = (sumSq / count) - (meanGray * meanGray);

                // Combine features (weighted)
                double feature = 0.6 * meanGray + 0.3 * meanEdge + 0.1 * variance;
                vector[gy * GRID_W + gx] = feature;
                if (feature > maxVal) maxVal = feature;
            }
        }

        // Normalize to [0,1]
        for (int i = 0; i < vector.length; i++) {
            vector[i] = clamp(vector[i] / maxVal);
        }

        return vector;
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
