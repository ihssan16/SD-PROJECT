package distributed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class NetworkServer extends Thread {

    private int port;
    private int nodeId; 
    private BlockingQueue<Message> messageQueue;

    public NetworkServer(int port) {
        this.port = port;
    }

    public NetworkServer(int port, int nodeId, BlockingQueue<Message> messageQueue) {
        this.port = port;
        this.nodeId = nodeId;
        this.messageQueue = messageQueue;
    }

    // public void start() {
    //     try {
    //         ServerSocket serverSocket = new ServerSocket(port);
    //         System.out.println("[SERVER] Listening on port " + port);

    //         while (true) {
    //             Socket clientSocket = serverSocket.accept();
    //             BufferedReader in = new BufferedReader(
    //                     new InputStreamReader(clientSocket.getInputStream())
    //             );

    //             String message = in.readLine();
    //             System.out.println("[RECEIVED] " + message);

    //             clientSocket.close();
    //         }

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

     public void run() {
        start();
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("[SERVER-" + nodeId + "] Listening on port " + port);  // CHANGED: Added nodeId

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );

                String message = in.readLine();
                
                String[] parts = message.split(":", 2);
                int senderId = Integer.parseInt(parts[0]);
                String payload = parts.length > 1 ? parts[1] : "";
                
                Message msg = new Message(senderId, payload);
                
                messageQueue.put(msg);
                
                System.out.println("[SERVER-" + nodeId + "] Received and queued: " + msg);

                clientSocket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

