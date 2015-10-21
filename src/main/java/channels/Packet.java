package channels;

import java.net.SocketAddress;

public interface Packet<T> {
    void pack(T payload);
    T unpack();

    void setRemoteAddress(SocketAddress remoteAddress);
    SocketAddress getRemoteAddress();

    Packet clone();
}
