package chatserver;

import channels.*;
import executors.EventDistributor;
import service.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class ServerSocketHandler implements Runnable {
    private final ServerSocket serverSocket;
    private final UserService userService;
    private final EventDistributor eventDistributor;

    public ServerSocketHandler(ServerSocket serverSocket, UserService userService, EventDistributor eventDistributor) {
        this.serverSocket = serverSocket;
        this.userService = userService;
        this.eventDistributor = eventDistributor;
    }

    @Override
    public void run() {
        while (true) {
            try {
                final Socket clientSocket = serverSocket.accept();

                final Channel channel;
                try {
                    channel = new MessageChannel(new Base64Channel(new TcpChannel(clientSocket)));
                } catch (ChannelException e) {
                    System.err.println("could not create a new channel for user socket: " + e);
                    clientSocket.close();
                    continue;
                }

                new ClientHandler(channel, userService, eventDistributor);
            } catch (IOException e) {
                System.err.println("could not accept client connection: " + e);
            }
        }
    }
}
