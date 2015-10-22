package commands;

public abstract class Event implements Message {
    @Override
    public long getMessageId() {
        return -1;
    }
}
