package distributed;

import java.util.Arrays;

public class VectorClock {
    private int[] timestamps;
    private int myId;

    public VectorClock(int numNodes, int myId) {
        this.timestamps = new int[numNodes];
        this.myId = myId; 
    }

    public synchronized void tick() {
        timestamps[myId - 1]++; 
    }

    public synchronized void update(int[] remoteTimestamps) {
        for (int i = 0; i < timestamps.length; i++) {
            timestamps[i] = Math.max(timestamps[i], remoteTimestamps[i]);
        }
        tick();
    }

    public synchronized int[] getTimestamps() {
        return Arrays.copyOf(timestamps, timestamps.length);
    }
}
