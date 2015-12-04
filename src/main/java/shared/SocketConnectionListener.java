package shared;

import channels.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class SocketConnectionListener extends RepeatingTask {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private final ServerSocket serverSocket;
    private final ChannelFactory channelFactory;
    private final HandlerFactory handlerFactory;

    public SocketConnectionListener(ServerSocket serverSocket, HandlerFactory handlerFactory) {
        this(serverSocket, handlerFactory, new ChannelFactory() {
            public MessageChannel createChannel(Socket clientSocket) throws ChannelException {
                return MessageChannelFactory.create(new SecureChannel(new Base64Channel(new TcpChannel(clientSocket))));
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
            cancel();
            return;
        }

        try {
            final Socket clientSocket = serverSocket.accept();

            final Channel channel;
            try {
                channel = channelFactory.createChannel(clientSocket);
            } catch (ChannelException e) {
                LOGGER.warning("could not create a new channel for user socket: " + e);
                clientSocket.close();
                return;
            }

            handlerFactory.createHandler(channel);
        } catch (IOException e) {
            if (!serverSocket.isClosed()) {
                LOGGER.warning("could not accept client connection: " + e);
            }
        }
    }

    @Override
    protected void onCancelled() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.warning("could not close server socket: " + e);
        }

        super.onCancelled();
    }
}
