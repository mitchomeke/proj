package lab5;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Discovery {
	private static Logger Log = Logger.getLogger(Discovery.class.getName());

	static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
	static final int DISCOVERY_PERIOD = 1000;
	static final int DISCOVERY_TIMEOUT = 5000;
	private static final String DELIMITER = "\t";

	private final Map<String, Map<URI, Long>> knownServers = new ConcurrentHashMap<>();
	private static Discovery instance;

	/**
	 * Announces a service periodically.
	 * Now calls getInstance() to ensure the listener is active.
	 */
	static public void announce(String serviceName, String domain, String serviceURI) {
		getInstance(); // Ensures listener/reaper threads are running
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
	static public URI[] knownUrisOf(String serviceName, int minReplies) {
		var res = getInstance().lookupAll(serviceName, minReplies);
		return res.toArray(new URI[0]);
	}

	public static synchronized Discovery getInstance() {
		if (instance == null) instance = new Discovery();
		return instance;
	}

	private Discovery() {
		this.startListener();
		this.startReaper();
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
}