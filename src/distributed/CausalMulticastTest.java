import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CausalMiddleware {

    // Identifiant de CE nœud (ex: 0, 1, 2...)
    private int localId;
    
    // Le buffer pour stocker les messages arrivés trop tôt
    // SynchronizedList n'est pas suffisant car on itère dessus, 
    // on utilisera 'synchronized' sur les méthodes.
    private List<Message> causalBuffer;
    
    // Références vers les modules des autres membres
    private VectorClock vectorClock; 
    private NetworkMock network;    

    public CausalMiddleware(int localId, VectorClock clock, NetworkMock net) {
        this.localId = localId;
        this.vectorClock = clock;
        this.network = net;
        this.causalBuffer = new ArrayList<>();
    }

    // ---------------------------------------------------------
    // 1. ENVOI (Broadcast)
    // Appelé quand l'utilisateur tape une commande > send "..."
    // ---------------------------------------------------------
    public synchronized void broadcast(String content) {
        // 1. On incrémente notre propre horloge logique
        vectorClock.increment(localId);

        // 2. On crée le message avec une COPIE de notre horloge actuelle
        int[] currentClockCopy = vectorClock.getCopy();
        Message msg = new Message(localId, content, currentClockCopy);

        // 3. On l'envoie à tout le monde (via Membre 1)
        System.out.println("[Causal] Broadcasting message: " + content + " with clock " + java.util.Arrays.toString(currentClockCopy));
        network.sendToAll(msg);
    }

    // ---------------------------------------------------------
    // 2. RÉCEPTION (Receive)
    // Appelé par le ReceiverThread (Membre 2) quand un socket reçoit un truc
    // ---------------------------------------------------------
    public synchronized void onReceive(Message msg) {
        // Ignorer ses propres messages si le réseau nous les renvoie (boucle locale)
        if (msg.senderId == localId) return;

        System.out.println("[Causal] Reçu de " + msg.senderId + " Clocks: Msg=" + java.util.Arrays.toString(msg.clock) + " | Local=" + java.util.Arrays.toString(vectorClock.getVector()));

        if (canDeliver(msg)) {
            deliver(msg);
            // Après une livraison, on vérifie si ça débloque le buffer
            checkBuffer();
        } else {
            System.out.println("   -> Trop tôt ! Mis en buffer. (Attente de messages précédents)");
            causalBuffer.add(msg);
        }
    }

    // ---------------------------------------------------------
    // 3. LOGIQUE CAUSALE (Can Deliver?)
    // Vérifie si on peut livrer le message maintenant
    // ---------------------------------------------------------
    private boolean canDeliver(Message msg) {
        int[] msgClock = msg.clock;
        int[] localClock = vectorClock.getVector();

        // Condition 1 : Le message doit être le PROCHAIN attendu de l'émetteur
        // msg[sender] == local[sender] + 1
        if (msgClock[msg.senderId] != localClock[msg.senderId] + 1) {
            return false;
        }

        // Condition 2 : Pour tous les autres nœuds k (k != sender),
        // on doit avoir tout vu ce que l'émetteur a vu.
        // msg[k] <= local[k]
        for (int k = 0; k < localClock.length; k++) {
            if (k != msg.senderId) {
                if (msgClock[k] > localClock[k]) {
                    return false;
                }
            }
        }

        return true;
    }

    // ---------------------------------------------------------
    // 4. LIVRAISON (Deliver)
    // ---------------------------------------------------------
    private void deliver(Message msg) {
        System.out.println("✅ [DELIVER] Message de Node " + msg.senderId + ": " + msg.content);
        
        // Mise à jour de l'horloge vectorielle locale
        // Normalement Membre 3 fait une méthode 'updateOnReceive'
        // Sinon, on incrémente juste l'index de l'émetteur
        vectorClock.updateSenderIndex(msg.senderId); 
    }

    // ---------------------------------------------------------
    // 5. GESTION DU BUFFER
    // Vérifie récursivement si des messages en attente peuvent être livrés
    // ---------------------------------------------------------
    private void checkBuffer() {
        boolean deliveredSomething = true;

        // On boucle tant qu'on livre quelque chose (car une livraison peut en débloquer une autre)
        while (deliveredSomething) {
            deliveredSomething = false;
            Iterator<Message> it = causalBuffer.iterator();

            while (it.hasNext()) {
                Message m = it.next();
                if (canDeliver(m)) {
                    deliver(m);
                    it.remove(); // On retire du buffer
                    deliveredSomething = true;
                    System.out.println("   -> Un message a été débloqué du buffer !");
                }
            }
        }
    }
}
