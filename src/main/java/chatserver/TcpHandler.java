package chatserver;

import executors.EventDistributor;
import service.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

class TcpHandler implements Runnable {
    private final ServerSocket serverSocket;
    private final UserService userService;
    private final EventDistributor eventDistributor;

    public TcpHandler(ServerSocket serverSocket, UserService userService, EventDistributor eventDistributor) {
        this.serverSocket = serverSocket;
        this.userService = userService;
        this.eventDistributor = eventDistributor;
    }

    @Override
    public void run() {
        List<ClientHandler> clientHandlers = new LinkedList<>();

        while (true) {
            try {
                final Socket clientSocket = serverSocket.accept();
                clientHandlers.add(new ClientHandler(clientSocket, userService, eventDistributor));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
