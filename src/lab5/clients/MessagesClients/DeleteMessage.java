package lab5.clients.MessagesClients;

import lab5.api.java.Messages;
import lab5.api.java.Result;
import lab5.clients.java.Clients;

import java.util.logging.Logger;

public class DeleteMessage {
    private static Logger Log = Logger.getLogger(DeleteMessage.class.getName());

    public static void main (String[] args){

        if (args.length != 4){
            System.err.println("Use java: " + DeleteMessage.class.getCanonicalName() + "name mid pwd domain");
            return;
        }

        String name = args[0];
        String mid = args[1];
        String pwd = args[2];
        String domain = args[3];

        Messages client = Clients.MessagesClient.get(domain);
        Result<Void> result = client.deleteMessage(name,mid,pwd);

        if (result.isOK()){
            Log.info("Message successfully deleted "+ mid);
        } else {
            Log.info("Unable to Delete Message " + mid);
        }
    }
}
