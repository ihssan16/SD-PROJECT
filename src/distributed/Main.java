package distributed;

public class Main {

    public static void main(String[] args) {

        // Node B (serveur)
        Node nodeB = new Node(2, "localhost", 5001);

        // Lancer le serveur du Node B
        NetworkServer server = new NetworkServer(nodeB.getPort());
        new Thread(() -> server.start()).start();

        // Pause pour laisser le serveur démarrer
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        // Node A envoie un message à Node B
        Node nodeA = new Node(1, "localhost", 5000);
        NetworkClient.sendTo(nodeB, "Hello from Node A");
    }
}
