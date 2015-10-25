package client;

import channels.*;
import cli.Shell;
import executors.RepeatingTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class PrivateMessageSocketListener extends RepeatingTask {
    private final ServerSocket serverSocket;
    private final Shell shell;
    private final ExecutorService executorService;

    public PrivateMessageSocketListener(ServerSocket serverSocket, Shell shell, ExecutorService executorService) {
        this.serverSocket = serverSocket;
        this.shell = shell;
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

            new PrivateMessageHandler(channel, shell, executorService);
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