package executors;

import channels.NetworkPacket;
import channels.Packet;
import messages.ErrorResponse;
import messages.Message;
import messages.Request;
import messages.Response;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

/**
 * Performs an asynchronous request
 *
 * The asynchronous request should be submitted to a executor service, the returned future can then be
 * used to get the result of the asynchronous request.
 */
public class AsyncRequest<RequestType extends Request, ResponseType extends Response> implements Callable<ResponseType> {
    private final RequestType request;
    private final Class<ResponseType> responseClass;
    private final MessageListener listener;
    private final MessageSender sender;

    public AsyncRequest(RequestType request, Class<ResponseType> responseClass, MessageListener listener, MessageSender sender) {
        this.request = request;
        this.responseClass = responseClass;
        this.listener = listener;
        this.sender = sender;
    }

    /**
     * Event handler which unblocks the async request thread when the response of a async request (request and response
     * have the same message id) is received
     */
    private class EventHandler implements MessageListener.EventHandler {
        private final Semaphore semaphore = new Semaphore(0);
        private Message response;

        @Override
        public void onMessageReceived(Packet<Message> packet) {
            Message message = packet.unpack();
            if (message.getMessageId() == request.getMessageId()) {
                response = message;
                signal();
            }
        }

        /**
         * Block until the response is received
         * @throws InterruptedException
         */
        public void waitForResponse() throws InterruptedException {
            semaphore.acquire();
        }

        /**
         * Unblock the thread which called waitForResponse (will unblock only one thread!)
         */
        private void signal() {
            semaphore.release();
        }

        /**
         * Response which was received, the response is only valid if the blocked thread was unblocked by signal(), if
         * the thread woke up due an InterruptedException, the return value will be unspecified.
         *
         * @return Received response
         */
        public Message getResponse() {
            return response;
        }
    }

    @Override
    public ResponseType call() throws Exception {
        final Packet<Message> requestPacket = new NetworkPacket<>();
        requestPacket.pack(request);
        sender.sendMessage(requestPacket);

        final EventHandler eventHandler = new EventHandler();
        try {
            listener.addEventHandler(eventHandler);
            eventHandler.waitForResponse();
        } catch (InterruptedException e) {
            System.err.println("timeout, event handler did not notify us so far ...");
        } finally {
            listener.removeEventHandler(eventHandler);
        }

        final Message response = eventHandler.getResponse();
        try {
            return responseClass.cast(response);
        } catch (ClassCastException e) {
            final ErrorResponse errorResponse = ErrorResponse.class.cast(response); // potential ClassCastExceptions are ok here
            throw new Exception(errorResponse.getReason());
        }
    }
}
