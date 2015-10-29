package shared;

import channels.*;
import executors.RepeatingTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketConnectionListener extends RepeatingTask {
    private final ServerSocket serverSocket;
    private final ChannelFactory channelFactory;
    private final HandlerFactory handlerFactory;

    public SocketConnectionListener(ServerSocket serverSocket, HandlerFactory handlerFactory) {
        this(serverSocket, handlerFactory, new ChannelFactory() {
            public MessageChannel createChannel(Socket clientSocket) throws ChannelException {
                return new MessageChannel(new Base64Channel(new TcpChannel(clientSocket)));
            }
        });
    }

    public SocketConnectionListener(ServerSocket serverSocket, HandlerFactory handlerFactory, ChannelFactory channelFactory) {
        this.serverSocket = serverSocket;
        this.handlerFactory = handlerFactory;
        this.channelFactory = channelFactory;
    }

    @Override
    protected void perform() {
        if (serverSocket.isClosed()) {
            cancel(true);
            return;
        }

        try {
            final Socket clientSocket = serverSocket.accept();

            final Channel channel;
            try {
                channel = channelFactory.createChannel(clientSocket);
            } catch (ChannelException e) {
                System.err.println("could not create a new channel for user socket: " + e);
                clientSocket.close();
                return;
            }

            handlerFactory.createHandler(channel);
        } catch (IOException e) {
            if (!serverSocket.isClosed()) {
                System.err.println("could not accept client connection: " + e);
            }
        }
    }

    @Override
    protected void onCancelled() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("could not close server socket: " + e);
        }

        super.onCancelled();
    }
}
