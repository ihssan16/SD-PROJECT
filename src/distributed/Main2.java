package distributed;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main2 {

    public static void main(String[] args) {
        int totalNodes = 3; // pour la taille du vecteur
        System.out.println("Testing Week 3 - Logical Clocks . . .\n");

        // 1. Définition des Noeuds
        Node nodeA = new Node(1, "localhost", 5001);
        Node nodeB = new Node(2, "localhost", 5002);
        Node nodeC = new Node(3, "localhost", 5003);

        // 2. Initialisation des Horloges
        LamportClock lpA = new LamportClock();
        VectorClock vcA = new VectorClock(totalNodes, nodeA.getId());

        LamportClock lpB = new LamportClock();
        VectorClock vcB = new VectorClock(totalNodes, nodeB.getId());

        LamportClock lpC = new LamportClock();
        VectorClock vcC = new VectorClock(totalNodes, nodeC.getId());

        // 3. Files d'attente
        BlockingQueue<Message> queueA = new LinkedBlockingQueue<>();
        BlockingQueue<Message> queueB = new LinkedBlockingQueue<>();
        BlockingQueue<Message> queueC = new LinkedBlockingQueue<>();

        // 4. Serveurs (mis à jour avec horloges)
        NetworkServer serverA = new NetworkServer(nodeA.getPort(), nodeA.getId(), queueA, lpA, vcA);
        NetworkServer serverB = new NetworkServer(nodeB.getPort(), nodeB.getId(), queueB, lpB, vcB);
        NetworkServer serverC = new NetworkServer(nodeC.getPort(), nodeC.getId(), queueC, lpC, vcC);

        // 5. Processeurs (Affichage)
        new ProcessorThread(nodeA.getId(), queueA).start();
        new ProcessorThread(nodeB.getId(), queueB).start();
        new ProcessorThread(nodeC.getId(), queueC).start();

        // Lancement des serveurs
        new Thread(() -> serverA.start()).start();
        new Thread(() -> serverB.start()).start();
        new Thread(() -> serverC.start()).start();

        try { 
            Thread.sleep(1000); 
        } catch (InterruptedException e) { 
            e.printStackTrace();
        }

        System.out.println("\nSending with Causal Stamps ...\n");

        // 6. Envois (mis à jour avec passage d'horloges pour le tick() et l'envoi)
        // A -> B
        NetworkClient.sendTo(nodeB, nodeA.getId(), "Hello from A", lpA, vcA);
        
        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

        // B -> C (Causalement lié à A car l'horloge de B a été mise à jour par A)
        NetworkClient.sendTo(nodeC, nodeB.getId(), "Hello from B", lpB, vcB);

        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

        // C -> A
        NetworkClient.sendTo(nodeA, nodeC.getId(), "Hello from C", lpC, vcC);

        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

        System.out.println("\nFinished");
        System.exit(0);
    }
}
