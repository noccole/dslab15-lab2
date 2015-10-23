package channels;

import java.io.*;
import java.net.Socket;

public class TcpChannel implements Channel<byte[]> {
    private final static String DEFAULT_ENCODING = "UTF-8";

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
    public void send(Packet<byte[]> packet) throws ChannelException {
        final String data;
        try {
            data = new String(packet.unpack(), DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new ChannelException(DEFAULT_ENCODING + " encoding failure", e);
        }

        out.println(data);
    }

    @Override
    public Packet<byte[]> receive() throws ChannelException {
        final String data;
        try {
            data = in.readLine();
        } catch (IOException e) {
            throw new ChannelException("could not read from stream", e);
        }

        final byte[] byteData;
        try {
            byteData = (data != null ? data.getBytes(DEFAULT_ENCODING) : null);
        } catch (UnsupportedEncodingException e) {
            throw new ChannelException(DEFAULT_ENCODING + " encoding failure", e);
        }

        Packet packet = new NetworkPacket();
        packet.pack(byteData);
        packet.setRemoteAddress(socket.getRemoteSocketAddress());
        return packet;
    }
}
