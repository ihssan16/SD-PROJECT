package distributed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkServer {

    private int port;

    public NetworkServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("[SERVER] Listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );

                String message = in.readLine();
                System.out.println("[RECEIVED] " + message);

                clientSocket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

