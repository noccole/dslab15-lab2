package shared;

import channels.ChannelException;
import channels.MessageChannel;

import java.net.Socket;

public interface ChannelFactory {
    MessageChannel createChannel(Socket clientSocket) throws ChannelException;
}
