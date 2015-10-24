package channels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpChannel implements Channel<byte[]> {
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
        out.println(Encoder.encodeByteArray(packet.unpack()));
    }

    @Override
    public Packet<byte[]> receive() throws ChannelException {
        final String data;
        try {
            data = in.readLine();
        } catch (IOException e) {
            throw new ChannelException("could not read from tcp stream", e);
        }

        Packet packet = new NetworkPacket();
        packet.pack(Encoder.decodeString(data));
        packet.setRemoteAddress(socket.getRemoteSocketAddress());
        return packet;
    }

    @Override
    public void close() throws ChannelException {
        try {
            socket.close();
        } catch (IOException e) {
            throw new ChannelException("error while closing tcp channel socket", e);
        }
    }
}
