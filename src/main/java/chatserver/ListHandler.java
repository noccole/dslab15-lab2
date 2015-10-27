package chatserver;

import channels.Channel;
import entities.User;
import messages.ListRequest;
import messages.ListResponse;
import service.UserService;
import shared.HandlerBase;
import states.State;
import states.StateException;
import states.StateResult;

import java.util.Map;
import java.util.concurrent.ExecutorService;

class ListHandler extends HandlerBase {
    private final UserService userService;

    public ListHandler(Channel channel, UserService userService, ExecutorService executorService) {
        this.userService = userService;

        init(channel, executorService, new StateListUsersService());
    }

    private class StateListUsersService extends State {
        @Override
        public StateResult handleListRequest(ListRequest request) throws StateException {
            final Map<String, User.Presence> userList = userService.getUserList();

            final ListResponse response = new ListResponse(request);
            response.setUserList(userList);

            return new StateResult(this, response);
        }
    }
}
