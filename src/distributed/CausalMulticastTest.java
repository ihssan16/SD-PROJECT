public class CausalMulticastTest {

    public static void main(String[] args) {

        int numberOfNodes = 3;

        // Création de 3 nœuds
        CausalMulticast nodeA = new CausalMulticast(0, numberOfNodes);
        CausalMulticast nodeB = new CausalMulticast(1, numberOfNodes);
        CausalMulticast nodeC = new CausalMulticast(2, numberOfNodes);

        System.out.println("=== TEST MULTICAST CAUSAL ===");

        // A envoie m1
        Message m1 = nodeA.broadcast("m1 from A");

        // B reçoit m1
        nodeB.onReceive(m1);

        // B envoie m2 après avoir reçu m1 (dépendance causale)
        Message m2 = nodeB.broadcast("m2 from B");

        // C reçoit m2 AVANT m1 (hors ordre réseau)
        System.out.println("\n--- C reçoit m2 avant m1 ---");
        nodeC.onReceive(m2);   // doit être BUFFERED

        // C reçoit ensuite m1
        System.out.println("\n--- C reçoit m1 ---");
        nodeC.onReceive(m1);   // doit livrer m1 PUIS m2 automatiquement

        System.out.println("\n=== FIN DU TEST ===");
    }
}
