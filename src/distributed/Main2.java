package distributed;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main2 {

    public static void main(String[] args) {
        
        System.out.println("Testing . . .\n");

        Node nodeA = new Node(1, "localhost", 5001);
        Node nodeB = new Node(2, "localhost", 5002);
        Node nodeC = new Node(3, "localhost", 5003);

        BlockingQueue<Message> queueA = new LinkedBlockingQueue<>();
        BlockingQueue<Message> queueB = new LinkedBlockingQueue<>();
        BlockingQueue<Message> queueC = new LinkedBlockingQueue<>();

        NetworkServer serverA = new NetworkServer(nodeA.getPort(), nodeA.getId(), queueA);
        NetworkServer serverB = new NetworkServer(nodeB.getPort(), nodeB.getId(), queueB);
        NetworkServer serverC = new NetworkServer(nodeC.getPort(), nodeC.getId(), queueC);

        ProcessorThread processorA = new ProcessorThread(nodeA.getId(), queueA);
        ProcessorThread processorB = new ProcessorThread(nodeB.getId(), queueB);
        ProcessorThread processorC = new ProcessorThread(nodeC.getId(), queueC);

        new Thread(() -> serverA.start()).start();
        new Thread(() -> serverB.start()).start();
        new Thread(() -> serverC.start()).start();
        
        processorA.start();
        processorB.start();
        processorC.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nSending ...\n");

        NetworkClient.sendTo(nodeB, nodeA.getId(), "Hello from A");

        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

        NetworkClient.sendTo(nodeC, nodeB.getId(), "Hello from B");

        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

        NetworkClient.sendTo(nodeA, nodeC.getId(), "Hello from C");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nFinished");

        // Ensure the application terminates, stopping the server/processor threads
        System.exit(0);
    }
}