package distributed;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class NetworkClient {

    public static void sendTo(Node node, int senderId, String message, LamportClock lamport, VectorClock vc) {
        try {
            Socket socket = new Socket(node.getIp(), node.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            int currentLamport = lamport.tick();
            vc.tick();
            String vectorStr = Arrays.toString(vc.getTimestamps()).replace(" ", "");
            
            out.println(senderId + "|" + currentLamport + "|" + vectorStr + "|" + message);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
