package client;

import channels.*;
import cli.Shell;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PrivateMessageSocketListener implements Runnable {
    private final ServerSocket serverSocket;
    private final Shell shell;

    public PrivateMessageSocketListener(ServerSocket serverSocket, Shell shell) {
        this.serverSocket = serverSocket;
        this.shell = shell;
    }

    @Override
    public void run() {
        while (true) {
            try {
                final Socket clientSocket = serverSocket.accept();

                Channel channel;
                try {
                    channel = new MessageChannel(new Base64Channel(new TcpChannel(clientSocket)));
                } catch (ChannelException e) {
                    System.err.println("could not create a new channel for user socket: " + e);
                    clientSocket.close();
                    continue;
                }

                new PrivateMessageHandler(channel, shell);
            } catch (IOException e) {
                System.err.println("could not accept client connection: " + e);
            }
        }
    }
}