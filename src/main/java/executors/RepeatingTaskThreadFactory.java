package executors;

import java.util.concurrent.ThreadFactory;

public class RepeatingTaskThreadFactory implements ThreadFactory {
    /**
     *
     * @param runnable Must be an instance of Repeating Task!
     * @return
     */
    @Override
    public Thread newThread(final Runnable runnable) {
        assert runnable instanceof RepeatingTask;

        final RepeatingTask task = (RepeatingTask)runnable;
        return new Thread(task) {
            @Override
            public void interrupt() {
                task.stop();
                super.interrupt();
            }
        };
    }
}
