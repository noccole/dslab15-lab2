package executors;

import channels.Packet;
import commands.Request;
import commands.Response;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandBus {
    private final static ExecutorService executorService = Executors.newCachedThreadPool();

    private RequestListener requestListener;
    private RequestHandler requestHandler;
    private ResponseSender responseSender;

    public CommandBus(RequestListener listener, final RequestHandler handler, final ResponseSender sender) {
        this.requestListener = listener;
        this.requestHandler = handler;
        this.responseSender = sender;

        handler.addEventHandler(new RequestHandler.EventHandler() {
            @Override
            public void onRequestHandled(Packet<Response> response) {
                sender.sendResponse(response);
            }
        });
        listener.addEventHandler(new RequestListener.EventHandler() {
            @Override
            public void onRequestReceived(Packet<Request> request) {
                handler.handleRequest(request);
            }
        });

        executorService.submit(sender);
        executorService.submit(handler);
        executorService.submit(listener);
    }
}
