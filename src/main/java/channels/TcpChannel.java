package channels;

import util.Utf8;

import java.io.*;
import java.net.Socket;

/**
 * Tcp Channel Implementation
 */
public class TcpChannel extends ChannelBase<byte[]> {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public TcpChannel(Socket socket) throws ChannelException {
        this.socket = socket;

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new ChannelException("Could not open tcp streams", e);
        }
    }

    @Override
    public void send(Packet<byte[]> packet) throws ChannelException {
        try {
            out.println(Utf8.encodeByteArray(packet.unpack()));
        } catch (UnsupportedEncodingException e) {
            throw new ChannelException("Could not send packet", e);
        }
    }

    @Override
    public Packet<byte[]> receive() throws ChannelException {
        final String data;
        try {
            data = in.readLine();
        } catch (IOException e) {
            close();
            throw new ChannelException("could not read from tcp stream", e);
        }

        if (data == null) {
            close();
            throw new ChannelException("tcp socket has been closed");
        }

        Packet packet = new NetworkPacket();
        packet.setRemoteAddress(socket.getRemoteSocketAddress());
        try {
            packet.pack(Utf8.decodeString(data));
        } catch (UnsupportedEncodingException e) {
            throw new ChannelException("Could not decode received data", e);
        }

        return packet;
    }

    @Override
    public void close() throws ChannelException {
        if (!socket.isClosed()) {
            try {
                socket.close();
                emitChannelClosed();
            } catch (IOException e) {
                throw new ChannelException("error while closing tcp channel socket", e);
            }
        }
    }
    
    @Override
    public Channel getChannel() {
    	return this;
    }
}
