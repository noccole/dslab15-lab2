package executors;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RepeatingTask implements Runnable {
    final AtomicBoolean stopped = new AtomicBoolean(false);

    protected abstract void perform();

    protected void onStopped() {

    }

    @Override
    public void run() {
        if (!stopped.get()) {
            perform();
        }
    }

    public void stop() {
        if (stopped.compareAndSet(false, true)) {
            onStopped();
        }
    }
}
