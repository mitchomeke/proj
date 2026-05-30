package lab5.clients.MessagesClients;

import lab5.api.Message;
import lab5.api.java.Messages;
import lab5.api.java.Result;
import lab5.api.java.Users;
import lab5.clients.UserClients.CreateUserClient;
import lab5.clients.java.Clients;
import lab5.clients.java.UsersClientFactory;
import lab5.clients.rest.RestMessagesClient;
import lab5.clients.rest.RestUsersClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;

public class CreateMessage {
    private static Logger Log = Logger.getLogger(CreateMessage.class.getName());

    public static void main(String[] args) throws IOException {
        if (args.length < 5){
            System.err.println("Use Java: " + CreateMessage.class.getCanonicalName() + "Sender pwd subject content domain destination1 destination2....");
            return;
        }

        String Sender = args[0];
        String pwd = args[1];
        String subject = args[2];
        String content = args[3];
        String domain = args[4];
        String[] destination = Arrays.copyOfRange(args,5,args.length);

        Message msg = new Message(Sender, Set.of(destination),subject,content);

        Messages client = Clients.MessagesClient.get(domain);

        Result<String> result = client.postMessage(pwd,msg);

        if (result.isOK()){
            Log.info("Message successfully Sent: " + Sender);
        } else {
            Log.info("Message not Sent " + result.error());
        }
    }
}
