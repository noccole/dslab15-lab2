package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

public class UdpChannel extends ChannelBase<byte[]> {
    private final DatagramSocket socket;
    private final int receiveBufferSize;

    public UdpChannel(DatagramSocket socket) throws ChannelException {
        this.socket = socket;

        try {
            receiveBufferSize = socket.getReceiveBufferSize();
        } catch (SocketException e) {
            throw new ChannelException("could not read receive buffer size", e);
        }
    }

    @Override
    public void send(Packet<byte[]> packet) throws ChannelException {
        final byte[] data = packet.unpack();

        if (data.length > receiveBufferSize) {
            throw new ChannelException("too much data for a datagram packet");
        }

        // use socket address from packet when possible
        SocketAddress socketAddress = packet.getRemoteAddress();
        if (socketAddress == null) {
            socketAddress = socket.getRemoteSocketAddress();
        }

        final DatagramPacket datagramPacket;
        try {
            datagramPacket = new DatagramPacket(data, data.length, socketAddress);
        } catch (SocketException e) {
            throw new ChannelException("could not generate datagram packet", e);
        }

        try {
            socket.send(datagramPacket);
        } catch (IOException e) {
            throw new ChannelException("could not send datagram packet", e);
        }
    }

    @Override
    public Packet receive() throws ChannelException {
        final byte[] data = new byte[receiveBufferSize];
        final DatagramPacket datagramPacket = new DatagramPacket(data, data.length);

        try {
            socket.receive(datagramPacket);
        } catch (IOException e) {
            close();
            throw new ChannelException("could not receive datagram packet", e);
        }

        Packet packet = new NetworkPacket();
        packet.pack(data);
        packet.setRemoteAddress(datagramPacket.getSocketAddress());
        return packet;
    }

    @Override
    public void close() throws ChannelException {
        socket.close();
        emitChannelClosed();
    }
}
