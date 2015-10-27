package shared;

import channels.Channel;

public interface HandlerFactory {
    HandlerBase createHandler(Channel channel);
}
