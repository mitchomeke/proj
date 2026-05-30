package lab5.clients.UserClients;

import lab5.api.User;
import lab5.api.java.Result;
import lab5.api.java.Users;
import lab5.clients.java.Clients;

import java.io.IOException;
import java.util.logging.Logger;

public class GetUserClient {
    private static Logger Log = Logger.getLogger(GetUserClient.class.getName());
    public static void main(String[] args) throws IOException {
        if (args.length != 3){
            System.err.println("Use: java "+ GetUserClient.class.getCanonicalName() + "name pwd domain");
            return;
        }
        /*

         */
        String name = args[0];
        String pwd = args[1];
        String domain = args[2];

        Users client = Clients.UsersClient.get(domain);
        Result<User> result = client.getUser(name,pwd);

        if (result.isOK()){
            Log.info("Successfully got user: "+ name);
        } else {
            Log.info("Failed to get User: " + name + result.error());
        }
    }
}
