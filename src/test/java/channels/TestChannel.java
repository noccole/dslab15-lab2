package channels;

public class TestChannel extends ChannelBase<byte[]> {
    private Packet<byte[]> lastPacket;

    private boolean exceptionOnSend = false;
    private boolean exceptionOnReceive = false;
    private boolean exceptionOnClose = false;

    public void setLastPacket(Packet<byte[]> lastPacket) {
        this.lastPacket = lastPacket;
    }

    public Packet<byte[]> getLastPacket() {
        return lastPacket;
    }

    public void setExceptionOnSend(boolean exceptionOnSend) {
        this.exceptionOnSend = exceptionOnSend;
    }

    public void setExceptionOnReceive(boolean exceptionOnReceive) {
        this.exceptionOnReceive = exceptionOnReceive;
    }

    public void setExceptionOnClose(boolean exceptionOnClose) {
        this.exceptionOnClose = exceptionOnClose;
    }

    @Override
    public void send(Packet<byte[]> packet) throws ChannelException {
        if (exceptionOnSend) {
            throw new ChannelException("Test send channel exception");
        }

        setLastPacket(packet);
    }

    @Override
    public Packet<byte[]> receive() throws ChannelException {
        if (exceptionOnReceive) {
            throw new ChannelException("Test receive channel exception");
        }

        return getLastPacket();
    }

    @Override
    public void close() throws ChannelException {
        if (exceptionOnClose) {
            throw new ChannelException("Test close channel exception");
        }
    }
}
