
package test3;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ImageVectorizer {
	private  WebcamUpdater webcamUpdater;
	private Thread thread;
	private BufferedImage image;

	public ImageVectorizer(WebcamUpdater webcamUpdater) {
		this.webcamUpdater = webcamUpdater;
		thread = new Thread(webcamUpdater);
    	thread.start();
    	
    	image = webcamUpdater.getImage();
	}

	public double[] getVectorizedImage(int width, int height) {
		image = webcamUpdater.getImage();
		if (image == null) {
		   // System.out.println("No webcam image yet, skipping frame...");
		    return new double[width * height]; // Don’t process this frame yet
		}
	//	System.out.println("image ready...");
		// Resize the image
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g2d = resizedImage.createGraphics();
		g2d.drawImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
		g2d.dispose();

		// Convert to grayscale and vectorize
		double[] vector = new double[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = resizedImage.getRGB(x, y);
				int gray = (rgb >> 16) & 0xFF; // Extract grayscale value
				vector[y * width + x] = gray / 255.0; // Normalize to [0, 1]
			}
		}

		return vector;
	}

	public static void main(String[] args) {
		WebcamUpdater webcamUpdater = new WebcamUpdater();
		Thread updaterThread = new Thread(webcamUpdater);
		updaterThread.start();

		ImageVectorizer vectorizer = new ImageVectorizer(webcamUpdater);

		// Example usage
		try {
			Thread.sleep(2000); // Wait for the webcam to start streaming
			double[] vector = vectorizer.getVectorizedImage(28, 28); // Resize to 28x28
			System.out.println("Vectorized Image: " + Arrays.toString(vector));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
