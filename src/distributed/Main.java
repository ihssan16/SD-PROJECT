package distributed;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    public static void main(String[] args) {
        int totalNodes = 2; // Pour le vecteur d'horloge

        Node nodeB = new Node(2, "localhost", 5001);
        BlockingQueue<Message> queueB = new LinkedBlockingQueue<>();
        
        LamportClock lamportB = new LamportClock();
        VectorClock vcB = new VectorClock(totalNodes, nodeB.getId());

        NetworkServer serverB = new NetworkServer(nodeB.getPort(), nodeB.getId(), queueB, lamportB, vcB);
        serverB.start(); 

        new ProcessorThread(nodeB.getId(), queueB).start();

        try { 
            Thread.sleep(1000); 
        } catch (InterruptedException e) {}

        Node nodeA = new Node(1, "localhost", 5000);
        
        LamportClock lamportA = new LamportClock();
        VectorClock vcA = new VectorClock(totalNodes, nodeA.getId());

        System.out.println("Node A envoie un message avec horloges...");

        // Node A envoie un message à Node B en passant ses horloges
        NetworkClient.sendTo(nodeB, nodeA.getId(), "Hello with clocks!", lamportA, vcA);
    }
}
