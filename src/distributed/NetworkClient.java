package distributed;

import java.io.PrintWriter;
import java.net.Socket;

public class NetworkClient {

    // public static void sendTo(Node node, String message) {
    //     try {
    //         Socket socket = new Socket(node.getIp(), node.getPort());
    //         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

    //         out.println(message);

    //         socket.close();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    public static void sendTo(Node node, int senderId, String message) {
        try {
            Socket socket = new Socket(node.getIp(), node.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println(senderId + ":" + message);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendTo(Node node, String message) {
        sendTo(node, 0, message);
    }
}

