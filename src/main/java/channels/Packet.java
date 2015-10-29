package channels;

import java.net.SocketAddress;

public interface Packet<T> {
    /**
     * Pack the message payload into the packet
     *
     * @param payload Payload which should be transferred
     */
    void pack(T payload);

    /**
     * Unpack the message payload of the packet
     *
     * @return Transferred payload
     */
    T unpack();

    /**
     * Set the remote socket address of this packet
     *
     * E.g. used for udp packet transmission
     *
     * @param remoteAddress Socket address of the remote node
     */
    void setRemoteAddress(SocketAddress remoteAddress);

    /**
     * @return Socket address of the remote node
     */
    SocketAddress getRemoteAddress();
}
