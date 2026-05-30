package lab5.clients.java;

import java.io.IOException;

public interface ClientFactory<T> {

	public T get(String domain) throws IOException;
}
