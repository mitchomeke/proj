package lab5.clients.UserClients;

import lab5.api.User;
import lab5.api.java.Result;
import lab5.api.java.Users;
import lab5.clients.java.Clients;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class SearchUserClient {
    private static Logger Log = Logger.getLogger(SearchUserClient.class.getName());

    public static void main(String[] args) throws IOException {
        if (args.length != 4){
            System.err.println("Use java: " + SearchUserClient.class.getCanonicalName() + " name pwd query domain");
            return;
        }
        String name = args[0];
        String pwd = args[1];
        String query = args[2];
        String domain = args[3];

        Users client = Clients.UsersClient.get(domain);
        Result<User> res = client.getUser(name,pwd);

        if (res.isOK()){
            Result<List<User>> result = client.searchUsers(query);
            if (result.isOK()){
                List<User> users = result.value();
                Log.info("Success: (" + users.size() + " users)");
                users.stream().forEach(u -> System.out.println(u));
            } else {
                Log.info("Unable to get users with this pattern " + result.error());
            }
        } else {
            Log.info("Unable to get user with this name: "+ name);
        }
    }
}
