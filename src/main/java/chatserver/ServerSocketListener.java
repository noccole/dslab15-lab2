package chatserver;

import channels.*;
import executors.EventDistributor;
import executors.RepeatingTask;
import service.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

class ServerSocketListener extends RepeatingTask {
    private final ServerSocket serverSocket;
    private final UserService userService;
    private final EventDistributor eventDistributor;
    private final ExecutorService executorService;

    public ServerSocketListener(ServerSocket serverSocket, UserService userService,
                                EventDistributor eventDistributor, ExecutorService executorService) {
        this.serverSocket = serverSocket;
        this.userService = userService;
        this.eventDistributor = eventDistributor;
        this.executorService = executorService;
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

            new ClientHandler(channel, userService, eventDistributor, executorService);
        } catch (IOException e) {
            System.err.println("could not accept client connection: " + e);
        }
    }
}
