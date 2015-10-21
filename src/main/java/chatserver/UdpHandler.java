package chatserver;

import channels.*;
import commands.*;
import executors.*;
import states.State;
import states.StateException;
import states.StateMachine;

import java.net.DatagramSocket;
import java.util.*;

public class UdpHandler {
    private final UserService userService;

    public UdpHandler(DatagramSocket socket, UserService userService) {
        this.userService = userService;

        Channel channel;
        try {
            channel = new CommandChannel(new UdpChannel(socket));
        } catch (ChannelException e) {
            e.printStackTrace();
            return;
        }

        final RequestListener listener = new ChannelRequestListener(channel);
        final ResponseSender sender = new ChannelResponseSender(channel);

        final State initialState = new StateListUsersService();
        final StateMachine stateMachine = new StateMachine(initialState);
        final RequestHandler handler = new StateMachineRequestHandler(stateMachine);

        new CommandBus(listener, handler, sender);
    }


    private class StateListUsersService extends State {
        @Override
        public StateResult handleListRequest(ListRequest command) throws StateException {
            final List<User> users = new LinkedList(userService.findAll());

            final Map<String, String> userStates = new TreeMap<>();
            for (User user : users) {
                userStates.put(user.getUsername(), user.getPresence().toString());
            }

            ListResponse response = new ListResponse();
            response.setUsers(userStates);

            return new StateResult(this, response);
        }

        @Override
        public String toString() {
            return "upd state static";
        }
    }
}
