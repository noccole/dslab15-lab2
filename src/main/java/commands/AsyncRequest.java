package commands;

import channels.NetworkPacket;
import channels.Packet;
import executors.MessageListener;
import executors.MessageSender;

import java.util.concurrent.Callable;

public class AsyncRequest<RequestType extends Request, ResponseType extends Response> implements Callable<ResponseType> {
    private final RequestType request;
    private final MessageListener listener;
    private final MessageSender sender;

    public AsyncRequest(RequestType request, MessageListener listener, MessageSender sender) {
        this.request = request;
        this.listener = listener;
        this.sender = sender;
    }

    private class EventHandler implements MessageListener.EventHandler {
        private final Object parent;
        private Message response;

        public EventHandler(Object parent) {
            this.parent = parent;
        }

        @Override
        public void onMessageReceived(Packet<Message> packet) {
            Message message = packet.unpack();
            if (message.getMessageId() == request.getMessageId()) {
                response = message;
                parent.notify();
            }
        }

        public Message getResponse() {
            return response;
        }
    }

    @Override
    public ResponseType call() throws Exception {
        final Packet<Message> requestPacket = new NetworkPacket<>();
        requestPacket.pack(request);
        sender.sendMessage(requestPacket);

        final EventHandler eventHandler = new EventHandler(this);
        try {
            listener.addEventHandler(eventHandler);
            wait(); // wait for a notify from event handler
        } catch (InterruptedException e) {
            System.err.println("timeout, event handler did not notify us so far ...");
            e.printStackTrace();
        } finally {
            listener.removeEventHandler(eventHandler);
        }

        Message response = eventHandler.getResponse();
        try {
            return (ResponseType) response;
        } catch (ClassCastException e) {
            ErrorResponse errorResponse = (ErrorResponse)response; // potential ClassCastExceptions are ok here
            throw new Exception(errorResponse.getReason());
        }
    }
}
