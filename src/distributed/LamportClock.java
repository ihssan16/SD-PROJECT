package distributed;

public class LamportClock {
    private int counter;

    public LamportClock() {
        this.counter = 0;
    }

    public synchronized int tick() {
        return ++counter;
    }

    public synchronized void update(int remoteValue) {
        this.counter = Math.max(this.counter, remoteValue) + 1;
    }

    public synchronized int getValue() {
        return counter;
    }
}
