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
	
	public static void main(String[] args) throws Exception{
// 1. Determine Port (use arg[0] or default to 8080)
		int port = (args.length > 0) ? Integer.parseInt(args[0]) : 8080;

		// 2. Determine Domain (use arg[1] or default to "ourorg")
		String domain = (args.length > 1) ? args[1] : "ourorg";

		Log.info(String.format("Starting Users Server on port %d, domain %s", port, domain));

		// 3. Start the server
		new RestUsersServer(port, Users.SERVICE_NAME, domain).start();
	}

}
