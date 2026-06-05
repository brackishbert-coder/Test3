package test3;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * Feature-based vectorizer with fixed 16×16 output.
 * Each of the 256 elements represents local brightness,
 * edge strength, and texture variance from a region of
 * the full webcam image.
 */
public class ClientVectorizer implements Runnable {

    private static final int GRID_W = 16;
    private static final int GRID_H = 16;
    String host = "localhost";
    int port = 5010;
	private double[] receiveds = new double[GRID_W*GRID_H];
    public ClientVectorizer() {
    }

    public double[] getFeatureVector() {
    if(receiveds.length<GRID_W*GRID_H) {
    	double[] temp = new double[GRID_W*GRID_H];
    	for (int i = 0; i < temp.length; i++) {
			temp[i]=0;
		}
    	for (int i = 0; i < receiveds.length; i++) {
			temp[i]=receiveds[i];
		}
    	
    	return temp;
    	
    }else if (receiveds.length==GRID_W*GRID_H) {
    	return receiveds;
    }else{
    	
    	double[] temp = new double[GRID_W*GRID_H];
    	for (int i = 0; i < temp.length; i++) {
			temp[i]=receiveds[i];
		}
    	return temp;
    }
        
    }



	@Override
	public void run() {
        System.out.println("VectorClient waiting for processed vectors...");
        while (true) {
            try {
                Socket socket = new Socket(host, port);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                System.out.println("Connected to VectorClientServer!");

                while (true) {
                    try {
                        receiveds = (double[]) in.readObject();
                        System.out.println("Received processed vector: " + Arrays.toString(receiveds));
                    } catch (EOFException e) {
                        System.out.println("Server closed connection, reconnecting...");
                        break;
                    }
                }

                socket.close();
                Thread.sleep(1000); // wait before reconnect

            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.err.println("Connection error: " + e.getMessage());
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }
		
	}
}
