package shared;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RepeatingTask implements RunnableFuture {
    public interface EventHandler {
        /**
         * Will be emitted when the repeating task was cancelled.
         */
        void onCancelled();
    }

    private final Set<EventHandler> eventHandlers = new HashSet<>();

    private boolean done = false;
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
                cancel(true);
            }
        }

        onExit();

        done = true;
    }

    /**
     * Cancel the repeating task execution, the task will not loop anymore.
     * @param b unused
     * @return True if the task was cancelled, false if it was already cancelled.
     */
    @Override
    public boolean cancel(boolean b) {
        if (cancelled.compareAndSet(false, true)) {
            onCancelled();
            emitCancelled();
            thread.interrupt();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Object get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
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
