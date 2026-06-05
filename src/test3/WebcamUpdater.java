package test3;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.Socket;

import javax.imageio.ImageIO;






public class WebcamUpdater implements Runnable {
	private final String serverAddress;
	private final int port;
	private static BufferedImage image;

	public WebcamUpdater() {
		this.serverAddress = "localhost";
		this.port = 5000;
	}

	@Override
	public void run() {

		while (true) {
			try (Socket socket = new Socket(serverAddress, port); InputStream inputStream = socket.getInputStream()) {

				System.out.println("Connected to server: " + serverAddress + ":" + port);

				byte[] buffer = new byte[65536]; // Adjust buffer size as needed
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 0, bytesRead);
					image = ImageIO.read(bais);


				}
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
			}
		}
	}


	public BufferedImage getImage() {
		return image;
	}



}
