public class CausalMulticastTest {

    public static void main(String[] args) {

        int N = 3;

        CausalMulticast nodeA = new CausalMulticast(0, N);
        CausalMulticast nodeB = new CausalMulticast(1, N);
        CausalMulticast nodeC = new CausalMulticast(2, N);

        // A envoie m1
        Message m1 = nodeA.broadcast("m1 from A");

        // B reçoit m1
        nodeB.onReceive(m1);

        // B envoie m2 après réception de m1
        Message m2 = nodeB.broadcast("m2 from B");

        // C reçoit m2 AVANT m1 (hors ordre réseau)
        System.out.println("\n--- C reçoit m2 avant m1 ---");
        nodeC.onReceive(m2);   // doit être bufferisé

        // C reçoit ensuite m1
        System.out.println("\n--- C reçoit m1 ---");
        nodeC.onReceive(m1);   // doit débloquer m2
    }
}
