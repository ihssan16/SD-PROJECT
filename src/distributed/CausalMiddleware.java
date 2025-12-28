package distributed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CausalMiddleware {

    private int myId;
    private VectorClock vectorClock;
    private INetwork network;
    
    // Le Buffer
    private List<Message> causalBuffer;
    
    // Variable pour arrêter proprement le thread si besoin
    private boolean running = true;

    public CausalMiddleware(int myId, VectorClock vectorClock, INetwork network) {
        this.myId = myId;
        this.vectorClock = vectorClock;
        this.network = network;
        this.causalBuffer = new ArrayList<>();

        // Le Thread de vérification
        // Ce thread tourne en fond et se réveille toutes les X ms
        Thread bufferWorker = new Thread(() -> {
            while (running) {
                try {
                    // "tente de vider le buffer toutes les X ms" (ici 1000ms)
                    Thread.sleep(1000); 
                    checkBufferPeriodic();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        // On le lance en mode Daemon pour qu'il ne bloque pas la fermeture du programme
        bufferWorker.setDaemon(true);
        bufferWorker.start();
    }

    // ---------------------------------------------------------
    // 1. ENVOI 
    // ---------------------------------------------------------
    public synchronized void broadcast(String payload) {
        vectorClock.tick();
        int[] currentVC = vectorClock.getTimestamps();
        Message msg = new Message(myId, payload, 0, currentVC);
        System.out.println("[Causal] Broadcast: " + payload + " | VC=" + java.util.Arrays.toString(currentVC));
        network.sendToAll(msg);
    }

    // ---------------------------------------------------------
    // 2. RÉCEPTION 
    // ---------------------------------------------------------
    public synchronized void onReceive(Message msg) {
        if (msg.getSenderId() == myId) return;

        System.out.println("[Causal] Reçu de " + msg.getSenderId() + 
                           " | MsgVC=" + java.util.Arrays.toString(msg.getVectorClock()));

        if (canDeliver(msg)) {
            deliver(msg);
            
        } else {
            System.out.println("   -> Mis en tampon (Attente du Thread...)");
            causalBuffer.add(msg);
        }
    }

    // ---------------------------------------------------------
    // 3. LOGIQUE CAUSALE 
    // ---------------------------------------------------------
    private boolean canDeliver(Message msg) {
        int[] msgVC = msg.getVectorClock();
        int[] localVC = vectorClock.getTimestamps();
        int senderIndex = msg.getSenderId() - 1; 

        if (msgVC[senderIndex] != localVC[senderIndex] + 1) return false;

        for (int k = 0; k < localVC.length; k++) {
            if (k != senderIndex) {
                if (msgVC[k] > localVC[k]) return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------
    // 4. LIVRAISON
    // ---------------------------------------------------------
    private void deliver(Message msg) {
        System.out.println("✅ [DELIVER] " + msg.getPayload() + " (de Node " + msg.getSenderId() + ")");
        vectorClock.update(msg.getVectorClock());
    }

    // ---------------------------------------------------------
    // 5. LE WORKER PÉRIODIQUE
    // ---------------------------------------------------------
    private synchronized void checkBufferPeriodic() {
        if (causalBuffer.isEmpty()) return;

        // System.out.println("[Thread Buffer] Vérification périodique...");
        
        Iterator<Message> it = causalBuffer.iterator();
        boolean deliveredSomething = false;

        while (it.hasNext()) {
            Message m = it.next();
            if (canDeliver(m)) {
                deliver(m);
                it.remove(); // On retire du buffer
                deliveredSomething = true;
                System.out.println("   -> Débloqué par le Thread périodique !");
            }
        }
        
    }
    
    public interface INetwork {
        void sendToAll(Message msg);
    }
}
