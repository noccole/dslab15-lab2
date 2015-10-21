package channels;

import java.net.SocketAddress;

abstract class PacketDecorator<T> implements Packet<T> {
    private final Packet<T> packet;

    public PacketDecorator(Packet packet) {
        this.packet = packet;
    }

    @Override
    public void pack(T payload) {
        packet.pack(payload);
    }

    @Override
    public T unpack() {
        return packet.unpack();
    }

    @Override
    public void setRemoteAddress(SocketAddress remoteAddress) {
        packet.setRemoteAddress(remoteAddress);
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return packet.getRemoteAddress();
    }

    @Override
    public String toString() {
        return packet.toString();
    }

    @Override
    public Packet clone() {
        return packet.clone();
    }
}
