package lab5.server.rest;

import org.glassfish.jersey.server.ResourceConfig;

import java.net.UnknownHostException;
import java.util.logging.Logger;

public class RestGatewayServer extends AbstractRestServer {

    private static Logger Log = Logger.getLogger(RestGatewayServer.class.getName());
    public static final int PORT = 8080;
    public static final String SERVICE_NAME = "gateway";

    RestGatewayServer(int port, String domain) throws UnknownHostException {
        super(Log, SERVICE_NAME, port, domain);
    }

    @Override
    void registerResources(ResourceConfig config) {
        config.register(RestGatewayResource.class);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: RestGatewayServer <port> <domain>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        String domain = args[1];
        new RestGatewayServer(port, domain).start();
    }

}
