package lab5.clients.MessagesClients;

import lab5.api.java.Messages;
import lab5.api.java.Result;
import lab5.clients.java.Clients;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class SearchInboxClient {
    private static Logger Log = Logger.getLogger(SearchInboxClient.class.getName());

    public static void main (String[] args) throws IOException {
        if (args.length != 4){
            System.err.println("Use Java: " + SearchInboxClient.class.getCanonicalName() + " name pwd query domain");
        }
        String name = args[0];
        String pwd = args[1];
        String query = args[2];
        String domain = args[3];

        Messages client = Clients.MessagesClient.get(domain);

        Result<List<String>> result = client.searchInbox(name,pwd,query);
        if (result.isOK()){
            Log.info("Successfully Retrieved " + result.value().size() + "Messages from user: "+ name + "with query: " + query);
            result.value().stream().forEach(u -> System.out.println(u));
        } else {
            Log.info("Unsuccessfully Retrieved Messages");
        }
    }
}
