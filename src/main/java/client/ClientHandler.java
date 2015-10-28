package client;

import channels.Channel;
import messages.*;
import states.State;
import states.StateException;
import states.StateResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class ClientHandler extends ClientHandlerBase {
    private final ExecutorService executorService;

    public ClientHandler(Channel channel, ExecutorService executorService) {
        this.executorService = executorService;

        init(channel, executorService, new StateHandleEvents());
    }

    public <RequestType extends Request, ResponseType extends Response> Future<ResponseType> asyncRequest(RequestType request) {
        final AsyncRequest<RequestType, ResponseType> asyncRequest = new AsyncRequest<>(request, getListener(), getSender());
        return executorService.submit(asyncRequest);
    }

    public <RequestType extends Request, ResponseType extends Response> ResponseType syncRequest(RequestType request) throws Exception {
        final Future<ResponseType> future = asyncRequest(request);
        return future.get(5, TimeUnit.SECONDS);
    }

    private class StateHandleEvents extends State {
        @Override
        public StateResult handleMessageEvent(MessageEvent event) throws StateException {
            emitMessageReceived(event.getUsername() + ": " + event.getMessage());

            return new StateResult(this);
        }

        @Override
        public StateResult handleExitEvent(ExitEvent event) throws StateException {
            return new StateResult(new StateExit());
        }
    }

    private class StateExit extends State {
        @Override
        public void onEntered() throws StateException {
            emitExit();
        }
    }
}
