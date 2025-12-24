package distributed;

public class Message {
    private int senderId;
    private String payload;
    private int lamportTimestamp; 
    private int[] vectorClock;   

    public Message(int senderId, String payload, int lamportTimestamp, int[] vectorClock) {
        this.senderId = senderId;
        this.payload = payload;
        this.lamportTimestamp = lamportTimestamp;
        this.vectorClock = (vectorClock != null) ? Arrays.copyOf(vectorClock, vectorClock.length) : null;
    }
    
    public int getSenderId() {
        return senderId;
    }

    public String getPayload() {
        return payload;
    }

    public int getLamportTimestamp() {
        return lamportTimestamp;
    }

    public int[] getVectorClock() {
        return vectorClock;
    }

    @Override
    public String toString() {
        return "Message{" +
                "senderId=" + senderId +
                ", payload='" + payload + '\'' +
                ", lamport=" + lamportTimestamp +
                ", vectorClock=" + Arrays.toString(vectorClock) +
                '}';
    }
}
