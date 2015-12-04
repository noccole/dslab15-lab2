package shared;

import channels.Channel;
import channels.MessageChannel;
import marshalling.MessageMarshaller;
import marshalling.SerializableMessageMarshaller;

public class MessageChannelFactory {
    public static MessageChannel create(Channel<byte[]> channel) {
        final MessageMarshaller messageMarshaller = new SerializableMessageMarshaller();
        return new MessageChannel(messageMarshaller, channel);
    }
}
