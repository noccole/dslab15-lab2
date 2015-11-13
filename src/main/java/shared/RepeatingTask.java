package shared;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RepeatingTask implements Runnable {
    public interface EventHandler {
        /**
         * Will be emitted when the repeating task was cancelled.
         */
        void onCancelled();
    }

    private final Set<EventHandler> eventHandlers = new HashSet<>();

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private Thread thread;

    /**
     * Will be called periodically as long as no InterruptedException happened and cancel was not called.
     *
     * @throws InterruptedException
     */
    protected abstract void perform() throws InterruptedException;

    /**
     * Will be called when cancel is invoked.
     *
     * Can be used e.g. to unblock perform.
     */
    protected void onCancelled() {

    }

    /**
     * Will be called after the thread run loop
     *
     * Can be used e.g. to perform cleanups
     */
    protected void onExit() {

    }

    @Override
    public void run() {
        thread = Thread.currentThread();

        while (!isCancelled()) {
            try {
                perform();
            } catch (InterruptedException e) {
                cancel();
            }
        }

        onExit();
    }

    /**
     * Cancel the repeating task execution, the task will not loop anymore.
     * @return True if the task was cancelled, false if it was already cancelled.
     */
    public boolean cancel() {
        if (cancelled.compareAndSet(false, true)) {
            onCancelled();
            emitCancelled();
            thread.interrupt();
            return true;
        } else {
            return false;
        }
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    private void emitCancelled() {
        synchronized (eventHandlers) {
            for (EventHandler eventHandler : eventHandlers) {
                eventHandler.onCancelled();
            }
        }
    }

    public void addEventHandler(EventHandler eventHandler) {
        synchronized (eventHandlers) {
            eventHandlers.add(eventHandler);
        }
    }

    public void removeEventHandler(EventHandler eventHandler) {
        synchronized (eventHandlers) {
            eventHandlers.remove(eventHandler);
        }
    }
}
