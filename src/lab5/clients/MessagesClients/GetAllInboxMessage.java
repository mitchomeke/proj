package lab5.clients.MessagesClients;

import lab5.api.java.Messages;
import lab5.api.java.Result;
import lab5.clients.java.Clients;

import java.util.List;
import java.util.logging.Logger;

public class GetAllInboxMessage {
    private static Logger Log = Logger.getLogger(GetAllInboxMessage.class.getName());

    public static void main(String[] args){
        if (args.length != 3){
            System.err.println("Use Java: " + GetAllInboxMessage.class.getCanonicalName() + "name pwd domain");
        }

        String name = args[0];
        String pwd = args[1];
        String domain = args[2];

        Messages client = Clients.MessagesClient.get(domain);

        Result<List<String>> result = client.getAllInboxMessages(name,pwd);
        if (result.isOK()){
            Log.info("Successfully Retrieved " + result.value().size() + "Messages");
            result.value().stream().forEach(u -> System.out.println(u));
        } else {
            Log.info("Unsuccessfully Retrieved Messages");
        }
    }
}
