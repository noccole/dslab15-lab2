package client;

import channels.*;
import shared.ChannelFactory;

import javax.crypto.Mac;
import java.net.Socket;

class PrivateMessageChannelFactory implements ChannelFactory {
    private final MacFactory macFactory;

    public PrivateMessageChannelFactory(MacFactory macFactory) {
        this.macFactory = macFactory;
    }

    @Override
    public MessageChannel createChannel(Socket clientSocket) throws ChannelException {
        final Mac hashMac;
        try {
            hashMac = macFactory.createMac();
        } catch (MacFactoryException e) {
            throw new ChannelException("Could not create mac for integrity channel", e);
        }

        return new MessageChannel(new Base64Channel(new IntegrityChannel(hashMac, new TcpChannel(clientSocket))));
    }
}
