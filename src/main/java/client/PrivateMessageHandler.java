package client;

import channels.Channel;
import commands.SendPrivateMessageRequest;
import commands.SendPrivateMessageResponse;
import states.State;
import states.StateException;
import states.StateResult;

import java.util.concurrent.ExecutorService;

class PrivateMessageHandler extends ClientHandlerBase {
    public PrivateMessageHandler(Channel channel, ExecutorService executorService) {
        init(channel, executorService, new StateOfferService());
    }

    private class StateOfferService extends State {
        @Override
        public StateResult handleSendPrivateMessageRequest(SendPrivateMessageRequest request) throws StateException {
            emitMessageReceived("[PRV] " + request.getSender() + ": " + request.getMessage());

            final SendPrivateMessageResponse response = new SendPrivateMessageResponse(request);

            return new StateResult(new StateShutdownService(), response);
        }
    }

    private class StateShutdownService extends State {
        @Override
        public void onEntered() throws StateException {
            stop();
        }
    }
}
