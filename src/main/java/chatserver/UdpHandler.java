package chatserver;

import channels.Channel;
import channels.ChannelException;
import channels.CommandChannel;
import channels.UdpChannel;
import commands.ListRequest;
import commands.ListResponse;
import executors.*;
import states.State;
import states.StateException;
import states.StateMachine;
import states.StateResult;

import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        public StateResult handleListRequest(ListRequest request) throws StateException {
            final List<User> users = new LinkedList(userService.findAll());

            final Map<String, String> userStates = new TreeMap<>();
            for (User user : users) {
                userStates.put(user.getUsername(), user.getPresence().toString());
            }

            ListResponse response = new ListResponse(request);
            response.setUsers(userStates);

            return new StateResult(this, response);
        }

        @Override
        public String toString() {
            return "upd state static";
        }
    }
}
