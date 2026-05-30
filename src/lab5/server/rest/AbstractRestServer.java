package lab5.server.rest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import lab5.Discovery;


public abstract class AbstractRestServer {
	private static final String SERVER_BASE_URI = "http://%s:%s/rest";
	
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}

	
	final protected int port;
	final protected Logger Log;
	final protected String service;
	final protected String serverURI;
	final protected String domain;
	String ip;
	
	protected AbstractRestServer(Logger log, String service, int port, String domain) throws UnknownHostException {
		this.Log = log;
		ip = InetAddress.getLocalHost().getHostAddress();
		this.port = port;
		this.service = service;
		this.serverURI = String.format(SERVER_BASE_URI,ip,port);
		this.domain = domain;

	}

	protected void start() throws InterruptedException, IOException {
		
		ResourceConfig config = new ResourceConfig();
		
		registerResources( config );
		
	//	var uri = URI.create("http://0.0.0.0:%d/rest".formatted(port));
	//	System.out.println( uri );
		
		JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config);
	//	discovery.lookup(service,domain);
		Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR,service,serverURI,domain);
		discovery.start();
		//Discovery.announce(service,domain,serverURI);
		
		Log.info(String.format("%s Server ready @ %s\n",  service, serverURI));
	}
	
	abstract void registerResources( ResourceConfig config );
}
