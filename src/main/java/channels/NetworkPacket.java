package channels;

import java.net.SocketAddress;

public class NetworkPacket<T> implements Packet<T> {
    private T payload;
    private SocketAddress remoteAddress;

    @Override
    public void pack(T payload) {
        this.payload = payload;
    }

    @Override
    public T unpack() {
        return payload;
    }

    @Override
    public void setRemoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public String toString() {
        return unpack().toString();
    }

    /**
     *
     * @return Network packet *without* payload
     */
    @Override
    public Packet clone() {
        Packet<T> packet = new NetworkPacket();
        packet.setRemoteAddress(remoteAddress);
        return packet;
    }
}
