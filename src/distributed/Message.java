package distributed;

public class Message {
    private int senderId;
    private String payload;

    public Message(int senderId, String payload) {
        this.senderId = senderId;
        this.payload = payload;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Message{senderId=" + senderId + ", payload='" + payload + "'}";
    }
}