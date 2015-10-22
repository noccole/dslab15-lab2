package chatserver;

import service.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpHandler implements Runnable {
    private final ServerSocket serverSocket;
    private final UserService userService;

    public TcpHandler(ServerSocket serverSocket, UserService userService) {
        this.serverSocket = serverSocket;
        this.userService = userService;
    }

    @Override
    public void run() {
        while (true) {
            try {
                final Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket, userService);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
