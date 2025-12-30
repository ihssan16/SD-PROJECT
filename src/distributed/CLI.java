package distributed;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Interface CLI (Command Line Interface) du système distribué
 * Responsable : Membre 5
 *
 * Permet à l'utilisateur d'interagir avec le nœud via la console
 */
public class CLI {

    public static void main(String[] args) {

        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║  SYSTÈME DISTRIBUÉ - MULTICAST CAUSAL  ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println();

        // Configuration : définir l'ID du nœud actuel
        int myId = getNodeIdFromArgs(args);

        // Configuration des nœuds du groupe
        Node node1 = new Node(1, "localhost", 5001);
        Node node2 = new Node(2, "localhost", 5002);
        Node node3 = new Node(3, "localhost", 5003);

        List<Node> group = Arrays.asList(node1, node2, node3);

        // Trouver le nœud actuel
        Node self = group.stream()
                .filter(n -> n.getId() == myId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Node ID " + myId + " not found"));

        // Créer et démarrer le nœud distribué
        DistributedNode node = new DistributedNode(self, group);
        node.start();

        // Petit délai pour que tout soit prêt
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n✅ Node " + myId + " is ready!");
        System.out.println("📍 Port: " + self.getPort());
        System.out.println("\nCommandes disponibles:");
        System.out.println("  send <message>  - Envoyer un message avec ordre causal");
        System.out.println("  status          - Afficher l'état actuel du Vector Clock");
        System.out.println("  help            - Afficher cette aide");
        System.out.println("  exit            - Quitter");
        System.out.println();

        // Boucle d'interaction
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Node-" + myId + " > ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split(" ", 2);
            String command = parts[0].toLowerCase();

            switch (command) {
                case "send":
                    if (parts.length < 2) {
                        System.out.println("❌ Usage: send <message>");
                    } else {
                        String message = parts[1];
                        node.send(message);
                        System.out.println("📤 Message envoyé: \"" + message + "\"");
                    }
                    break;

                case "status":
                    int[] vc = node.getVectorClock();
                    System.out.println("🕒 Vector Clock: " + Arrays.toString(vc));
                    break;

                case "help":
                    printHelp();
                    break;

                case "exit":
                case "quit":
                    System.out.println("👋 Arrêt du nœud...");
                    System.exit(0);
                    break;

                default:
                    System.out.println("❌ Commande inconnue: " + command);
                    System.out.println("   Tapez 'help' pour la liste des commandes");
            }
        }
    }

    /**
     * Récupère l'ID du nœud depuis les arguments
     */
    private static int getNodeIdFromArgs(String[] args) {
        if (args.length == 0) {
            System.out.println("⚠️  Aucun ID spécifié, utilisation de l'ID par défaut: 1");
            System.out.println("   Usage: java distributed.CLI <node_id>");
            System.out.println();
            return 1;
        }

        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("❌ ID invalide: " + args[0]);
            System.exit(1);
            return -1;
        }
    }

    private static void printHelp() {
        System.out.println("\n📖 AIDE - COMMANDES DISPONIBLES");
        System.out.println("─────────────────────────────────");
        System.out.println("  send <message>  : Diffuse un message à tous les nœuds");
        System.out.println("                    avec garantie d'ordre causal");
        System.out.println();
        System.out.println("  status          : Affiche l'état actuel du Vector Clock");
        System.out.println("                    Format: [VC_node1, VC_node2, VC_node3]");
        System.out.println();
        System.out.println("  help            : Affiche cette aide");
        System.out.println();
        System.out.println("  exit / quit     : Arrête le nœud et quitte");
        System.out.println();
    }
}