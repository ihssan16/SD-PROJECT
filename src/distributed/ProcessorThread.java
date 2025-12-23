package distributed;

import java.util.concurrent.BlockingQueue;

/**
 * ProcessorThread - Processes messages from the queue
 */
public class ProcessorThread extends Thread {
    private int nodeId;
    private BlockingQueue<Message> messageQueue;

    public ProcessorThread(int nodeId, BlockingQueue<Message> messageQueue) {
        this.nodeId = nodeId;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        System.out.println("[PROCESSOR-" + nodeId + "] Started");

        while (true) {
            try {
                // Take message from queue (blocks until available)
                Message message = messageQueue.take();
                
                // Process it
                System.out.println("[DELIVERED-" + nodeId + "] From Node " + 
                                 message.getSenderId() + ": " + message.getPayload());
                
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}