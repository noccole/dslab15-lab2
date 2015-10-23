package chatserver;

import channels.*;
import commands.ListRequest;
import commands.ListResponse;
import entities.User;
import executors.*;
import service.UserService;
import states.State;
import states.StateException;
import states.StateMachine;
import states.StateResult;

import java.net.DatagramSocket;
import java.util.Map;

public class UdpHandler {
    private final UserService userService;

    public UdpHandler(DatagramSocket socket, UserService userService) {
        this.userService = userService;

        Channel channel;
        try {
            channel = new MessageChannel(new Base64Channel(new UdpChannel(socket)));
        } catch (ChannelException e) {
            e.printStackTrace();
            return;
        }

        final MessageListener listener = new ChannelMessageListener(channel);
        final MessageSender sender = new ChannelMessageSender(channel);

        final State initialState = new StateListUsersService();
        final StateMachine stateMachine = new StateMachine(initialState);
        final MessageHandler handler = new StateMachineMessageHandler(stateMachine);

        final CommandBus localBus = new CommandBus();
        localBus.addMessageSender(sender);
        localBus.addMessageHandler(handler);
        localBus.addMessageListener(listener);
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
            return "upd state static";
        }
    }
}
