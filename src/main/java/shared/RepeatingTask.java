package shared;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RepeatingTask implements RunnableFuture {
    private boolean done = false;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

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

    @Override
    public void run() {
        while (!isCancelled()) {
            try {
                perform();
            } catch (InterruptedException e) {
                cancel(true);
            }
        }

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
}
