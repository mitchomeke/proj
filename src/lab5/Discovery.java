package lab5;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

//This is a fake Discovery implementation... 

public class Discovery {
	private static Logger Log = Logger.getLogger(Discovery.class.getName());

	// Multicast address and port (Standard for these labs)
	private static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
	private static final int DISCOVERY_PERIOD = 1000; // 1 second
	private static final int DISCOVERY_TIMEOUT = 5000; // 5 seconds

	private final Map<String, Map<URI, Long>> knownServers = new ConcurrentHashMap<>();
	private static Discovery instance;


	// Assuming your map is structured to handle multiple services:
// private final Map<String, Map<URI, Long>> knownServers = new ConcurrentHashMap<>();

	static public URI[] knownUrisOf(String serviceName, int minReplies) {
		// 1. Get the map of URIs for this specific service
		Map<URI, Long> serverInstances = instance.knownServers.get(serviceName);

		// 2. Check if the service even exists in our records
		if (serverInstances == null) {
			throw new IllegalArgumentException("Service not found: " + serviceName);
		}

		List<URI> listOfURIs = new ArrayList<>();
		long currentTime = System.currentTimeMillis();

		// 3. Filter for "Fresh" servers (those that announced in the last 10 seconds)
		// Note: 1000ms (1s) might be too strict; usually, 5000-10000ms is safer for network lag.
		for (Map.Entry<URI, Long> entry : serverInstances.entrySet()) {
			if (currentTime - entry.getValue() < 10000) {
				listOfURIs.add(entry.getKey());
			}
		}

		// 4. Check if we met the minimum required replies
		if (listOfURIs.size() < minReplies) {
			throw new IllegalArgumentException("Available URIs (" + listOfURIs.size() +
					") is less than required (" + minReplies + ")");
		}

		return listOfURIs.toArray(new URI[0]);
	}

	public static synchronized Discovery getInstance() {
		if (instance == null) instance = new Discovery();
		return instance;
	}
	private Discovery() {
		this.startListener(); // Start listening for others as soon as we are created
	}

	static public void announce(String serviceName, String serviceURI) {
		Log.info(String.format("Announcing: %s at %s",serviceName, serviceURI));
		String message = String.format("%s:%s", serviceName, serviceURI);
		byte[] pktBytes = message.getBytes();
		DatagramPacket pkt = new DatagramPacket(pktBytes, pktBytes.length, DISCOVERY_ADDR);

		// Schedule periodic announcements
		new Timer(true).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try (DatagramSocket ms = new DatagramSocket()) {
					ms.send(pkt);
				} catch (Exception e) { e.printStackTrace(); }
			}
		}, 0, DISCOVERY_PERIOD);
	}
	// --- LOOKUP (Used by Resources/Clients) ---
	public URI lookup(String domain, String serviceName) {
		String key = domain + ":" + serviceName;
		long start = System.currentTimeMillis();

		while (System.currentTimeMillis() - start < DISCOVERY_TIMEOUT) {
			// 1. Check if the service name exists in our records
			Map<URI, Long> instances = knownServers.get(key);

			if (instances != null && !instances.isEmpty()) {
				long now = System.currentTimeMillis();

				// 2. Find the first URI that hasn't timed out (e.g., 10 seconds)
				for (Map.Entry<URI, Long> entry : instances.entrySet()) {
					if (now - entry.getValue() < 10000) {
						return entry.getKey(); // Return the first fresh server we find
					}
				}
			}

			// 3. If nothing found or all were expired, wait and try again
			try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
		}

		throw new RuntimeException("Discovery timeout: could not find " + key);
	}
	// --- LISTENER (Internal) ---
	private void startListener() {
		// --- 1. THE NETWORK LISTENER THREAD ---
		new Thread(() -> {
			try (MulticastSocket ms = new MulticastSocket(DISCOVERY_ADDR.getPort())) {
				NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
				ms.joinGroup(DISCOVERY_ADDR, ni);

				while (true) {
					byte[] buffer = new byte[65535];
					DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
					ms.receive(pkt);

					String msg = new String(pkt.getData(), 0, pkt.getLength());
					String[] parts = msg.split(":");

					// FIXED: Expecting 2 parts (serviceName and URI)
					if (parts.length >= 2) {
						String serviceKey = parts[0]; // e.g., "users:fct"
						// Handle cases where the URI might contain colons (like http://)
						String uriString = msg.substring(serviceKey.length() + 1);
						URI serverUri = URI.create(uriString);

						// Update the map with the current timestamp
						knownServers.computeIfAbsent(serviceKey, k -> new ConcurrentHashMap<>())
								.put(serverUri, System.currentTimeMillis());
					}
				}
			} catch (Exception e) {
				Log.severe("Discovery Listener failed: " + e.getMessage());
			}
		}).start();

		// --- 2. THE REAPER (CLEANUP) THREAD ---
		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(5000); // Check every 5 seconds
					long now = System.currentTimeMillis();

					// Remove expired URIs (older than 10 seconds)
					knownServers.values().forEach(instances -> {
						instances.entrySet().removeIf(entry -> (now - entry.getValue()) > 10000);
					});

					// Clean up service keys that no longer have any active servers
					knownServers.entrySet().removeIf(entry -> entry.getValue().isEmpty());

				} catch (InterruptedException e) {
					break;
				}
			}
		}).start();
	}
}
