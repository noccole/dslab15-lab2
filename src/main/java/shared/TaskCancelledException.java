package shared;

public class TaskCancelledException extends Exception {
    public TaskCancelledException(String message) {
        super(message);
    }

    public TaskCancelledException(String message, Throwable cause) {
        super(message, cause);
    }
}
