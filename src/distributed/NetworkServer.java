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
    private LamportClock lamport; 
    private VectorClock vc;      

    public NetworkServer(int port, int nodeId, BlockingQueue<Message> messageQueue, LamportClock lamport, VectorClock vc) {
        this.port = port;
        this.nodeId = nodeId;
        this.messageQueue = messageQueue;
        this.lamport = lamport;
        this.vc = vc;
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[SERVER-" + nodeId + "] Listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String rawData = in.readLine();

                if (rawData != null) {
                    String[] parts = rawData.split("\\|", 4);
                    int senderId = Integer.parseInt(parts[0]);
                    int remoteLamport = Integer.parseInt(parts[1]);
                    
                    String vcStr = parts[2].replace("[", "").replace("]", "");
                    int[] remoteVC = Arrays.stream(vcStr.split(",")).mapToInt(Integer::parseInt).toArray();
                    String payload = parts[3];
                    
                    lamport.update(remoteLamport);
                    vc.update(remoteVC);

                    // Création du message enrichi avec les horloges reçues
                    Message msg = new Message(senderId, payload, remoteLamport, remoteVC);
                    messageQueue.put(msg);

                    System.out.println("[SERVER-" + nodeId + "] Received: " + msg);
                }
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
