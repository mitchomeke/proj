package lab5.clients.UserClients;

import lab5.api.User;
import lab5.api.java.Result;
import lab5.api.java.Users;
import lab5.clients.java.Clients;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;

public class UpdateUserClient {
    private static Logger Log = Logger.getLogger(UpdateUserClient.class.getName());

    public static void main (String[] args){
        if (args.length < 6){
            System.err.println("Use: java: " + UpdateUserClient.class.getCanonicalName() + "name pwd newPwd newDisplayName newDomain phonenumber1 phonenumber2 .....");
            return;
        }
        String name = args[0];
        String pwd = args[1];
        String newPwd = args[2];
        String newDisplayName = args[3];
        String newDomain = args[4];
        String[] phoneNumbers = Arrays.copyOfRange(args,4,args.length);

        Users client = Clients.UsersClient.get(newDomain);
        User user = new User(name,newPwd,newDisplayName,newDomain,null,Set.of(phoneNumbers));

        Result<User> result = client.updateUser(name,pwd,user);

        if (result.isOK()){
            Log.info("User was successfully updated: " + name);
        } else {
            Log.info("User was not updated: " + name + result.error());
        }
    }
}
