package client;

import channels.Channel;
import messages.SendPrivateMessageRequest;
import messages.SendPrivateMessageResponse;
import messages.TamperedRequest;
import messages.TamperedResponse;
import shared.HandlerManager;
import shared.TamperedResponseBuilder;
import states.State;
import states.StateException;
import states.StateResult;

import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

class PrivateMessageHandler extends ClientHandlerBase {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    public PrivateMessageHandler(Channel channel, ExecutorService executorService, HandlerManager handlerManager) {
        init(channel, executorService, handlerManager, new StateOfferService());
    }

    private class StateOfferService extends State {
        @Override
        public StateResult handleSendPrivateMessageRequest(SendPrivateMessageRequest request) throws StateException {
            LOGGER.info("PrivateMessageHandler::StateOfferService::handleSendPrivateMessageRequest with parameters: " + request);

            emitMessageReceived(request.getSender() + ": " + request.getMessage());

            final SendPrivateMessageResponse response = new SendPrivateMessageResponse(request);

            return new StateResult(this, response);
        }

        @Override
        public StateResult handleTamperedRequest(TamperedRequest request) throws StateException {
            final TamperedResponse response = TamperedResponseBuilder.getResponseFor(request);
            emitTamperedMessageReceived(response.getReason());

            return new StateResult(this, response);
        }
    }
}
