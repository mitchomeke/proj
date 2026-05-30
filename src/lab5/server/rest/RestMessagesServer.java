package lab5.server.rest;

import lab5.Discovery;
import lab5.api.java.Messages;
import lab5.server.java.JavaMessages;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class RestMessagesServer extends AbstractRestServer{
    public static final int PORT = 7070;

    private static Logger Log = Logger.getLogger(RestMessagesServer.class.getName());
    RestMessagesServer(int port, String service, String domain) throws UnknownHostException {
        super(Log, Messages.SERVICE_NAME, PORT, domain);
    }
    @Override
    void registerResources(ResourceConfig config) {
        config.register(RestMessagesResource.class);
    }
    public static void main (String[] args) throws IOException, InterruptedException {
        String domain = args[1];
        new RestMessagesServer(PORT,Messages.SERVICE_NAME,domain).start();
    }
}
