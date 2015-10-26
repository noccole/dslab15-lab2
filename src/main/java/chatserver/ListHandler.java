package chatserver;

import channels.Channel;
import channels.ChannelException;
import channels.Packet;
import commands.ListRequest;
import commands.ListResponse;
import commands.Message;
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
    private final Channel channel;
    private final UserService userService;

    private final MessageListener listener;
    private final MessageSender sender;
    private final MessageHandler handler;

    public ListHandler(Channel channel, UserService userService, ExecutorService executorService) {
        this.channel = channel;
        this.userService = userService;

        listener = new ChannelMessageListener(channel);
        sender = new ChannelMessageSender(channel);

        final State initialState = new StateListUsersService();
        final StateMachine stateMachine = new StateMachine(initialState);
        handler = new StateMachineMessageHandler(stateMachine);

        listener.addEventHandler(new MessageListener.EventHandler() {
            @Override
            public void onMessageReceived(Packet<Message> message) {
                handler.handleMessage(message);
            }
        });
        handler.addEventHandler(new MessageHandler.EventHandler() {
            @Override
            public void onMessageHandled(Packet<Message> message, Packet<Message> result) {
                sender.sendMessage(result);
            }
        });

        executorService.submit(sender);
        executorService.submit(handler);
        executorService.submit(listener);
    }

    public void stop() {
        try {
            channel.close();
        } catch (ChannelException e) {
            System.err.println("could not close channel: " + e);
        }

        listener.cancel(true);
        handler.cancel(true);
        sender.cancel(true);
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
