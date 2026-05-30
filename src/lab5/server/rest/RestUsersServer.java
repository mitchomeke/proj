package lab5.server.rest;

import java.net.UnknownHostException;
import java.util.logging.Logger;

import lab5.api.rest.RestUsers;
import org.glassfish.jersey.server.ResourceConfig;

import lab5.api.java.Users;

public class RestUsersServer extends AbstractRestServer {

	public static final int PORT = 8080;

	private static Logger Log = Logger.getLogger(RestUsersServer.class.getName());

	RestUsersServer(int port, String service, String domain) throws UnknownHostException {
		super(Log, Users.SERVICE_NAME, PORT,domain);
	}
	
	@Override
	void registerResources(ResourceConfig config) {
		config.register(RestUsersResource.class);
	}

	public static void main(String[] args) throws Exception {
	//	int port = args.length > 0 ? Integer.parseInt(args[0]) : PORT;

		String domain = (args.length > 0 && !args[0].equalsIgnoreCase("default")) ? args[0] : "ourorg";

	//	Log.info(String.format("Starting %s Server: port %d, domain %s", Users.SERVICE_NAME, PORT, domain));
		new RestUsersServer(PORT,Users.SERVICE_NAME, domain).start();
	}

}
