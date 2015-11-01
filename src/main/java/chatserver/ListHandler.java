package chatserver;

import channels.Channel;
import entities.User;
import messages.ListRequest;
import messages.ListResponse;
import service.UserService;
import shared.HandlerBase;
import shared.HandlerManager;
import states.State;
import states.StateException;
import states.StateResult;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

class ListHandler extends HandlerBase {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private final UserService userService;

    public ListHandler(Channel channel, UserService userService, ExecutorService executorService, HandlerManager handlerManager) {
        this.userService = userService;

        init(channel, executorService, handlerManager, new StateListUsersService());
    }

    private class StateListUsersService extends State {
        @Override
        public StateResult handleListRequest(ListRequest request) throws StateException {
            LOGGER.info("ListHandler::StateListUsersService::handleListRequest with parameters: " + request);

            final Map<String, User.Presence> userList = userService.getUserList();

            final ListResponse response = new ListResponse(request);
            response.setUserList(userList);

            return new StateResult(this, response);
        }
    }
}
