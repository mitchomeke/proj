package lab5;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Discovery {
	private static Logger Log = Logger.getLogger(Discovery.class.getName());

	public static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
	static final int DISCOVERY_PERIOD = 1000;
	static final int DISCOVERY_TIMEOUT = 5000;
	private static final String DELIMITER = "\t";
	static final int MAX_DATAGRAM_SIZE = 65536;

	// String = ServiceKey (e.g. "users.ourorg")
// URI = Server Address
// Long = Last seen timestamp
	private static final Map<String, Map<URI, Long>> knownServers = new ConcurrentHashMap<>();
	private static Discovery instance;

	private final InetSocketAddress addr;
	private final String serviceName;
	private final String serviceURI;
	private final String domain;
	private final MulticastSocket ms;

	/**
	 * @param serviceName the name of the service to announce
	 * @param serviceURI  an uri string - representing the contact endpoint of the
	 *                    service being announced
	 * @throws IOException
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public Discovery(InetSocketAddress addr, String serviceName, String serviceURI, String domain) throws SocketException, UnknownHostException, IOException {
		this.addr = addr;
		this.serviceName = serviceName;
		this.serviceURI = serviceURI;
        this.domain = domain;

        if (this.addr == null) {
			throw new RuntimeException("A multinet address has to be provided.");
		}

		this.ms = new MulticastSocket(addr.getPort());
		this.ms.joinGroup(addr, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
	}

	public void start() {
		//If this discovery instance was initialized with information about a service, start the thread that makes the
		//periodic announcement to the multicast address.
		if (this.serviceName != null && this.serviceURI != null && this.domain != null) {

			Log.info(String.format("Starting Discovery announcements on: %s for: %s on domain: %s-> %s", addr, serviceName,
					domain,serviceURI));
			String serviceKey = serviceName.toLowerCase() + "." + domain.toLowerCase();

			byte[] announceBytes = String.format("%s%s%s", serviceKey, DELIMITER, serviceURI).getBytes();
			DatagramPacket announcePkt = new DatagramPacket(announceBytes, announceBytes.length, addr);

			try {
				// start thread to send periodic announcements
				new Thread(() -> {
					for (;;) {
						try {
							ms.send(announcePkt);
							Thread.sleep(DISCOVERY_PERIOD);
						} catch (Exception e) {
							e.printStackTrace();
							// do nothing
						}
					}
				}).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// start thread to collect announcements received from the network.
		new Thread(() -> {
			DatagramPacket pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);
			for (;;) {
				try {
					pkt.setLength(MAX_DATAGRAM_SIZE);
					ms.receive(pkt);
					String msg = new String(pkt.getData(), 0, pkt.getLength());
					String[] msgElems = msg.split(DELIMITER);
					if (msgElems.length == 2) { // periodic announcement
						System.out.printf("FROM %s (%s) : %s\n", pkt.getAddress().getHostName(),
								pkt.getAddress().getHostAddress(), msg);

						String serviceKey = msgElems[0]; // Ensure this matches your lookup key!
						URI serverUri = URI.create(msgElems[1]);

						knownServers.computeIfAbsent(serviceKey, k -> new ConcurrentHashMap<>())
								.put(serverUri, System.currentTimeMillis());

						synchronized (knownServers) {
							knownServers.notifyAll();
						}
					}
				} catch (IOException e) {
					// do nothing
				}
			}
		}).start();
	}

	/**
	 * Announces a service periodically.
	 * Now calls getInstance() to ensure the listener is active.
	 */
	static public void announce(String serviceName, String domain, String serviceURI) {
		//getInstance(); // Ensures listener/reaper threads are running
		Log.info(String.format("Starting periodic announcements for: %s@%s", serviceName, domain));

		String message = String.format("%s@%s%s%s", serviceName, domain, DELIMITER, serviceURI);
		byte[] pktBytes = message.getBytes();
		DatagramPacket pkt = new DatagramPacket(pktBytes, pktBytes.length, DISCOVERY_ADDR);

		new Timer(true).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try (DatagramSocket ds = new DatagramSocket()) {
					ds.send(pkt);
				} catch (Exception e) {
					Log.severe("Failed to send announcement: " + e.getMessage());
				}
			}
		}, 0, DISCOVERY_PERIOD);
	}

	/**
	 * Finds URIs for a given service.
	 */
	public static URI[] knownUrisOf(String serviceKey, int minReplies) throws IOException {
		long startTime = System.currentTimeMillis(); // Define the start point here

		synchronized (knownServers) {
			while (true) {
				Map<URI, Long> instances = knownServers.get(serviceKey);
				List<URI> freshUris = new ArrayList<>();

				if (instances != null) {
					long now = System.currentTimeMillis();
					instances.forEach((uri, timestamp) -> {
						if (now - timestamp < 10000) freshUris.add(uri);
					});
				}

				if (freshUris.size() >= minReplies) {
					return freshUris.toArray(new URI[0]);
				}

				// Check if we have exceeded the 5-second timeout
				long elapsed = System.currentTimeMillis() - startTime;
				if (elapsed >= DISCOVERY_TIMEOUT) {
					throw new IOException("Discovery timeout for: " + serviceKey);
				}

				try {
					// Wait for the remainder of the timeout period
					knownServers.wait(DISCOVERY_TIMEOUT - elapsed);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IOException("Discovery interrupted");
				}
			}
		}
	}





/*
public static synchronized Discovery getInstance() throws IOException {
		if (instance == null) instance = new Discovery();
		return instance;
	}



	private List<URI> lookupAll(String serviceKey, int minReplies) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < DISCOVERY_TIMEOUT) {
			Map<URI, Long> instances = knownServers.get(serviceKey);
			if (instances != null) {
				List<URI> freshUris = new ArrayList<>();
				long now = System.currentTimeMillis();

				instances.forEach((uri, timestamp) -> {
					if (now - timestamp < 10000) freshUris.add(uri);
				});

				if (freshUris.size() >= minReplies) return freshUris;
			}
			try { Thread.sleep(500); } catch (InterruptedException e) { break; }
		}
		throw new RuntimeException("Discovery timeout for: " + serviceKey);
	}

	private void startListener() {
		new Thread(() -> {
			try (MulticastSocket ms = new MulticastSocket(DISCOVERY_ADDR.getPort())) {
				NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
				ms.joinGroup(DISCOVERY_ADDR, ni);

				while (true) {
					byte[] buffer = new byte[65535];
					DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
					ms.receive(pkt);

					String msg = new String(pkt.getData(), 0, pkt.getLength());
					// FIX: Split by Tab, not colon!
					String[] parts = msg.split(DELIMITER);

					if (parts.length == 2) {
						String serviceKey = parts[0];
						URI serverUri = URI.create(parts[1]);

						knownServers.computeIfAbsent(serviceKey, k -> new ConcurrentHashMap<>())
								.put(serverUri, System.currentTimeMillis());
					}
				}
			} catch (Exception e) {
				Log.severe("Discovery Listener failed: " + e.getMessage());
			}
		}).start();
	}
	private void startReaper() {
		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(5000);
					long now = System.currentTimeMillis();
					knownServers.values().forEach(uris -> uris.entrySet().removeIf(e -> (now - e.getValue()) > 10000));
					knownServers.entrySet().removeIf(e -> e.getValue().isEmpty());
				} catch (InterruptedException e) { break; }
			}
		}).start();
	}

		// Add this to Discovery.java to fix the "cannot find symbol" errors
	public URI lookup(String serviceName, String domain) {
		String key = serviceName + "@" + domain;
		// We reuse our existing lookupAll logic to find at least 1 URI
		return lookupAll(key, 1).get(0);
	}

 */
private List<URI> lookupAll(String serviceKey, int minReplies) {
	long start = System.currentTimeMillis();
	while (System.currentTimeMillis() - start < DISCOVERY_TIMEOUT) {
		// Correcting the cast: it's now a Map of URI to Long
		Map<URI, Long> instances = knownServers.get(serviceKey);

		if (instances != null) {
			List<URI> freshUris = new ArrayList<>();
			long now = System.currentTimeMillis();

			// Only pick servers that announced themselves in the last 10 seconds
			instances.forEach((uri, timestamp) -> {
				if (now - timestamp < 10000) {
					freshUris.add(uri);
				}
			});

			if (freshUris.size() >= minReplies) return freshUris;
		}

		try {
			synchronized(knownServers) {
				knownServers.wait(500);
			}
		} catch (InterruptedException e) { break; }
	}
	throw new RuntimeException("Discovery timeout for: " + serviceKey);
}
public URI lookup(String serviceName, String domain) {
	String key = serviceName.toLowerCase() + "0." + domain.toLowerCase();
	// We reuse our existing lookupAll logic to find at least 1 URI
	return lookupAll(key, 1).get(0);

}
}