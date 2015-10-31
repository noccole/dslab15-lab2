package client;

import channels.Channel;
import messages.SendPrivateMessageRequest;
import messages.SendPrivateMessageResponse;
import states.State;
import states.StateException;
import states.StateResult;

import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

class PrivateMessageHandler extends ClientHandlerBase {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    public PrivateMessageHandler(Channel channel, ExecutorService executorService) {
        init(channel, executorService, new StateOfferService());
    }

    private class StateOfferService extends State {
        @Override
        public StateResult handleSendPrivateMessageRequest(SendPrivateMessageRequest request) throws StateException {
            LOGGER.info("PrivateMessageHandler::StateOfferService::handleSendPrivateMessageRequest with parameters: " + request);

            emitMessageReceived(request.getSender() + ": " + request.getMessage());

            final SendPrivateMessageResponse response = new SendPrivateMessageResponse(request);

            return new StateResult(this, response);
        }
    }
}
