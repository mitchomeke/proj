package lab5;

import java.net.*;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

//This is a fake Discovery implementation... 

public class Discovery {
	private static Logger Log = Logger.getLogger(Discovery.class.getName());

	// Multicast address and port (Standard for these labs)
	private static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
	private static final int DISCOVERY_PERIOD = 1000; // 1 second
	private static final int DISCOVERY_TIMEOUT = 5000; // 5 seconds

	private final Map<String, URI> knownServers = new ConcurrentHashMap<>();
	private static Discovery instance;


	static public URI[] knownUrisOf(String serviceName, int minReplies) {

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

		// Wait and retry until we find it or timeout
		while (System.currentTimeMillis() - start < DISCOVERY_TIMEOUT) {
			if (knownServers.containsKey(key)) {
				return knownServers.get(key);
			}
			try { Thread.sleep(500); } catch (InterruptedException e) {}
		}
		throw new RuntimeException("Discovery timeout: could not find " + key);
	}
	// --- LISTENER (Internal) ---
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
					String[] parts = msg.split(":");
					if (parts.length == 3) {
						// key = domain:serviceName, value = URI
						knownServers.put(parts[0] + ":" + parts[1], URI.create(parts[2]));
					}
				}
			} catch (Exception e) { e.printStackTrace(); }
		}).start();
	}
}
