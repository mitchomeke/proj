package lab5.server.rest;

import lab5.api.java.Messages;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.UnknownHostException;
import java.util.logging.Logger;

public class RestMessagesServer extends AbstractRestServer{
    public static final int PORT = 8080;

    private static Logger Log = Logger.getLogger(RestMessagesServer.class.getName());
    RestMessagesServer(int port, String service, String domain) throws UnknownHostException {
        super(Log, Messages.SERVICE_NAME, PORT, domain);
    }
    @Override
    void registerResources(ResourceConfig config) {
        config.register(RestMessagesResource.class);
    }
    public static void main (String[] args) throws UnknownHostException {
        int port = Integer.parseInt(args[0]);
        String domain = args[1];
        new RestMessagesServer(port,Messages.SERVICE_NAME,domain);
    }
}
