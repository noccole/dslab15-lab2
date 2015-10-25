package executors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RepeatingTask implements RunnableFuture {
    private boolean done = false;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    protected abstract void perform() throws InterruptedException;

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

    @Override
    public boolean cancel(boolean b) {
        return cancelled.compareAndSet(false, true);
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
