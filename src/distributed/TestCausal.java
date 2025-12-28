package distributed;
import java.util.Arrays;

public class TestCausal{

    public static void main(String[] args) {
        System.out.println("=== TEST CAUSALITÉ 3 NOEUDS  ===\n");

        int myId = 3; 
        int numNodes = 3;
        VectorClock vClock = new VectorClock(numNodes, myId);
        
        CausalMiddleware.INetwork networkMock = msg -> { };
        
        CausalMiddleware middleware = new CausalMiddleware(myId, vClock, networkMock);

        System.out.println("Nous sommes le Nœud 3. État initial : " + Arrays.toString(vClock.getTimestamps()));

        // M1
        int[] clockM1 = {1, 0, 0};
        Message m1 = new Message(1, "M1: Premier message", 0, clockM1);

        // M2
        int[] clockM2 = {1, 1, 0};
        Message m2 = new Message(2, "M2: Réponse", 0, clockM2);

        System.out.println("--- 1. Arrivée de M2 (Trop tôt) ---");
        middleware.onReceive(m2);
        
        System.out.println("--- 2. Arrivée de M1 ---");
        middleware.onReceive(m1);

        System.out.println("--- 3. Pause 2s pour le Thread ---");
        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        System.out.println("État final : " + Arrays.toString(vClock.getTimestamps()));
    }
}
