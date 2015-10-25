package chatserver;

import channels.*;
import executors.EventDistributor;
import executors.RepeatingTask;
import service.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class ServerSocketHandler extends RepeatingTask {
    private final ServerSocket serverSocket;
    private final UserService userService;
    private final EventDistributor eventDistributor;

    public ServerSocketHandler(ServerSocket serverSocket, UserService userService, EventDistributor eventDistributor) {
        this.serverSocket = serverSocket;
        this.userService = userService;
        this.eventDistributor = eventDistributor;
    }

    @Override
    protected void perform() {
        try {
            final Socket clientSocket = serverSocket.accept();

            final Channel channel;
            try {
                channel = new MessageChannel(new Base64Channel(new TcpChannel(clientSocket)));
            } catch (ChannelException e) {
                System.err.println("could not create a new channel for user socket: " + e);
                clientSocket.close();
                return;
            }

            new ClientHandler(channel, userService, eventDistributor);
        } catch (IOException e) {
            System.err.println("could not accept client connection: " + e);
        }
    }

    @Override
    protected void onStopped() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("could not close server socket: " + e);
        }

        super.onStopped();
    }
}
