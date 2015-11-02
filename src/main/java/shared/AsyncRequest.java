package shared;

import channels.NetworkPacket;
import channels.Packet;
import messages.ErrorResponse;
import messages.Message;
import messages.Request;
import messages.Response;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

/**
 * Performs an asynchronous request
 *
 * The asynchronous request should be submitted to a executor service, the returned future can then be
 * used to get the result of the asynchronous request.
 */
public class AsyncRequest<RequestType extends Request, ResponseType extends Response> implements Callable<ResponseType> {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

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
            final Message message = packet.unpack();
            if (message.getMessageId() == request.getMessageId()) {
                response = message;
                signal();
            }
        }

        @Override
        public void onCancelled() {
            response = null;
            signal();
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
         * @return Received response (null if no response received)
         */
        public Message getResponse() {
            return response;
        }
    }

    @Override
    public ResponseType call() throws Exception {
        if (sender.isCancelled() || listener.isCancelled()) {
            throw new Exception("Could not create a request, sender and listener must not be cancelled!");
        }

        final EventHandler eventHandler = new EventHandler();
        listener.addEventHandler(eventHandler);

        // send request
        final Packet<Message> requestPacket = new NetworkPacket<>();
        requestPacket.pack(request);
        try {
            sender.sendMessage(requestPacket);
        } catch (TaskCancelledException e) {
            listener.removeEventHandler(eventHandler);
            throw new Exception("Could not send request", e);
        }

        // wait for response
        try {
            eventHandler.waitForResponse();
        } catch (InterruptedException e) {
            LOGGER.warning("timeout, event handler did not notify us so far ...");
        } finally {
            listener.removeEventHandler(eventHandler);
        }

        final Message response = eventHandler.getResponse();
        if (response == null) {
            throw new Exception("No response received");
        }

        try {
            return responseClass.cast(response);
        } catch (ClassCastException e) {
            final ErrorResponse errorResponse;
            try {
                errorResponse = ErrorResponse.class.cast(response);
            } catch (ClassCastException e1) {
                throw new Exception("Unknown response type!", e1);
            }
            throw new Exception(errorResponse.getReason());
        }
    }
}
