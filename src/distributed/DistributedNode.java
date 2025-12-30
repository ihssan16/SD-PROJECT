package distributed;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Classe centrale d'intégration du système distribué
 * Responsable : Membre 5
 *
 * Rôle :
 * - Instancier et connecter tous les modules
 * - Lancer les threads nécessaires
 * - Fournir une API simple au CLI
 */
public class DistributedNode {

    private Node self;
    private List<Node> group;

    // Horloges
    private LamportClock lamportClock;
    private VectorClock vectorClock;

    // Communication réseau
    private NetworkServer server;

    // File thread-safe
    private BlockingQueue<Message> messageQueue;

    // Thread de traitement
    private ProcessorThread processor;

    // Middleware causal
    private CausalMiddleware causalMiddleware;

    /**
     * Constructeur : configure le nœud distribué
     * @param self le nœud actuel
     * @param group la liste de tous les nœuds (y compris self)
     */
    public DistributedNode(Node self, List<Node> group) {
        this.self = self;
        this.group = group;

        // Initialisation des horloges
        this.lamportClock = new LamportClock();
        this.vectorClock = new VectorClock(group.size(), self.getId());

        // File d'attente thread-safe
        this.messageQueue = new LinkedBlockingQueue<>();

        // Serveur réseau
        this.server = new NetworkServer(
                self.getPort(),
                self.getId(),
                messageQueue,
                lamportClock,
                vectorClock
        );

        // Interface réseau pour le middleware causal
        CausalMiddleware.INetwork network = msg -> {
            // Broadcast à tous les autres nœuds
            for (Node node : group) {
                if (node.getId() != self.getId()) {
                    NetworkClient.sendTo(
                            node,
                            self.getId(),
                            msg.getPayload(),
                            lamportClock,
                            vectorClock
                    );
                }
            }
        };

        // Middleware causal
        this.causalMiddleware = new CausalMiddleware(
                self.getId(),
                vectorClock,
                network
        );

        // Thread de traitement avec callback vers le middleware causal
        this.processor = new ProcessorThread(self.getId(), messageQueue) {
            @Override
            public void run() {
                System.out.println("[PROCESSOR-" + self.getId() + "] Started with causal delivery");

                while (true) {
                    try {
                        Message message = messageQueue.take();
                        // Transmet au middleware causal pour vérification
                        causalMiddleware.onReceive(message);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        };
    }

    /**
     * Démarre tous les threads du nœud
     */
    public void start() {
        System.out.println("[NODE-" + self.getId() + "] Starting...");

        // Lancer le serveur réseau
        server.start();
        // Lancer le processeur
        processor.start();

        System.out.println("[NODE-" + self.getId() + "] Ready on port " + self.getPort());
    }

    /**
     * API pour envoyer un message (utilisée par le CLI)
     * @param payload le contenu du message
     */
    public void send(String payload) {
        causalMiddleware.broadcast(payload);
    }

    /**
     * Retourne l'ID de ce nœud
     */
    public int getId() {
        return self.getId();
    }

    /**
     * Retourne le Vector Clock actuel (pour debug)
     */
    public int[] getVectorClock() {
        return vectorClock.getTimestamps();
    }
}