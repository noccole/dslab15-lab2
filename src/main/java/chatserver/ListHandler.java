package chatserver;

import channels.Channel;
import commands.ListRequest;
import commands.ListResponse;
import entities.User;
import executors.*;
import service.UserService;
import states.State;
import states.StateException;
import states.StateMachine;
import states.StateResult;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public class ListHandler {
    private final UserService userService;

    public ListHandler(Channel channel, UserService userService, ExecutorService executorService) {
        this.userService = userService;

        final MessageListener listener = new ChannelMessageListener(channel);
        final MessageSender sender = new ChannelMessageSender(channel);

        final State initialState = new StateListUsersService();
        final StateMachine stateMachine = new StateMachine(initialState);
        final MessageHandler handler = new StateMachineMessageHandler(stateMachine);

        final CommandBus localBus = new CommandBus();
        localBus.addMessageSender(sender);
        localBus.addMessageHandler(handler);
        localBus.addMessageListener(listener);

        executorService.submit(sender);
        executorService.submit(handler);
        executorService.submit(listener);
    }

    private class StateListUsersService extends State {
        @Override
        public StateResult handleListRequest(ListRequest request) throws StateException {
            final Map<String, User.Presence> userList = userService.getUserList();

            final ListResponse response = new ListResponse(request);
            response.setUserList(userList);

            return new StateResult(this, response);
        }

        @Override
        public String toString() {
            return "state list users service";
        }
    }
}
