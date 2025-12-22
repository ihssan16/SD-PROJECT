package distributed;

public class Node {
    private int id;
    private String ip;
    private int port;

    public Node(int id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Node{id=" + id + ", ip='" + ip + "', port=" + port + "}";
    }
}
