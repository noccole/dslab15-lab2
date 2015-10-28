package client;

import channels.Channel;
import entities.User;
import executors.AsyncRequest;
import messages.*;
import states.State;
import states.StateException;
import states.StateResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

class ClientHandler extends ClientHandlerBase {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private final ExecutorService executorService;

    public ClientHandler(Channel channel, ExecutorService executorService) {
        this.executorService = executorService;

        init(channel, executorService, new StateHandleEvents());
    }

    public <RequestType extends Request, ResponseType extends Response> Future<ResponseType> asyncRequest(RequestType request, Class<ResponseType> responseClass) {
        LOGGER.info("ClientHandler::asyncRequest with parameters: " + request);

        final AsyncRequest<RequestType, ResponseType> asyncRequest = new AsyncRequest<>(request, responseClass, getListener(), getSender());
        return executorService.submit(asyncRequest);
    }

    public <RequestType extends Request, ResponseType extends Response> ResponseType syncRequest(RequestType request, Class<ResponseType> responseClass) throws Exception {
        LOGGER.info("ClientHandler::syncRequest with parameters: " + request);

        final Future<ResponseType> future = asyncRequest(request, responseClass);
        return future.get(5, TimeUnit.SECONDS);
    }

    private class StateHandleEvents extends State {
        @Override
        public StateResult handleMessageEvent(MessageEvent event) throws StateException {
            LOGGER.info("ClientHandler::StateHandleEvents::handleMessageEvent with parameters: " + event);

            emitMessageReceived(event.getUsername() + ": " + event.getMessage());

            return new StateResult(this);
        }

        @Override
        public StateResult handleExitEvent(ExitEvent event) throws StateException {
            LOGGER.info("ClientHandler::StateHandleEvents::handleExitEvent with parameters: " + event);

            return new StateResult(new StateExit());
        }

        @Override
        public StateResult handleUserStateChangedEvent(UserPresenceChangedEvent event) throws StateException {
            LOGGER.info("ClientHandler::StateHandleEvents::handleUserStateChangedEvent with parameters: " + event);

            emitMessageReceived(event.getUsername() + " went " + (event.getPresence() == User.Presence.Offline ? "offline" : "online") + " ...");

            return new StateResult(this);
        }
    }

    private class StateExit extends State {
        @Override
        public void onEntered() throws StateException {
            LOGGER.info("ClientHandler::StateExit::onEntered");

            emitExit();
        }
    }
}
