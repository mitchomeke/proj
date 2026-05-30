package lab5.clients.MessagesClients;

import lab5.api.Message;
import lab5.api.java.Messages;
import lab5.api.java.Result;
import lab5.clients.java.Clients;

import java.io.IOException;
import java.util.logging.Logger;

public class GetInboxMessage {
    private static Logger Log = Logger.getLogger(GetInboxMessage.class.getName());
    public static void main(String[] args) throws IOException {
        if (args.length != 4){
            System.err.println("Use java: " + GetInboxMessage.class.getCanonicalName() + "name pwd mid domain");
        }

        String name = args[0];
        String pwd = args[1];
        String mid = args[2];
        String domain = args[3];

        Messages client = Clients.MessagesClient.get(domain);
        Result<Message> result = client.getInboxMessage(name,mid,pwd);

        if (result.isOK()){
            Log.info("Message from inbox successfully gotten "+ mid);
        } else {
            Log.info("Unable to get message from inbox" + mid);
        }
    }
}
