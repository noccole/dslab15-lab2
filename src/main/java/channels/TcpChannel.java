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
            throw new ChannelException("Could not open streams", e);
        }
    }

    @Override
    public void send(Packet packet) {
        out.println(packet.unpack());
    }

    @Override
    public Packet receive() throws ChannelException {
        String data;
        try {
            data = in.readLine();
        } catch (IOException e) {
            throw new ChannelException("could not read from stream", e);
        }

        Packet packet = new NetworkPacket();
        packet.pack(data != null ? data.getBytes() : null);
        packet.setRemoteAddress(socket.getRemoteSocketAddress());
        return packet;
    }
}
