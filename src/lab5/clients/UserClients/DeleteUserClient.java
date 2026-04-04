package lab5.clients.UserClients;

import lab5.api.User;
import lab5.api.java.Result;
import lab5.api.java.Users;
import lab5.clients.java.Clients;

import java.util.logging.Logger;

public class DeleteUserClient {
    private static Logger Log = Logger.getLogger(DeleteUserClient.class.getName());
    public static void main(String[] args) {
        if (args.length != 3){
            System.err.println( "Use: java " + DeleteUserClient.class.getCanonicalName() + "name pwd domain");
            return;
        }

        String name = args[0];
        String pwd = args[1];
        String domain = args[2];

        Users client = Clients.UsersClient.get(domain);


        Result<User> result = client.deleteUser(name,pwd);
        if (result.isOK()){
            Log.info("Deleted User "+ name);
        } else {
            Log.info("Unable to Delete User with error "+ result.error());
        }


    }
}
