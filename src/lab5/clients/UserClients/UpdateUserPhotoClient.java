package lab5.clients.UserClients;

import jakarta.ws.rs.client.Client;
import lab5.api.User;
import lab5.api.java.Result;
import lab5.api.java.Users;
import lab5.clients.java.Clients;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class UpdateUserPhotoClient {
    private static Logger Log = Logger.getLogger(UpdateUserPhotoClient.class.getName());

    public static void main (String[] args) throws IOException {
        if (args.length != 4){
            System.err.println("Use Java: " + UpdateUserPhotoClient.class.getCanonicalName() + "name pwd fileName domain");
            return;
        }

        String name = args[0];
        String pwd = args[1];
        String fileName = args[2];
        String domain = args[3];

        Path PhotoFilePath = Paths.get(fileName);
        if(!Files.exists(PhotoFilePath)){
            System.err.println("FileName: " + fileName + "does not exist");
            System.exit(1);
        }
        byte[] photoData = Files.readAllBytes(PhotoFilePath);

        if (photoData.length == 0){
            System.err.println("The Photo Dats is empty (has zero bytes)");
            System.exit(1);
        }

        Users client = Clients.UsersClient.get(domain);
        Result<User> result =  client.updateUserPhoto(name,pwd,photoData);

        if (result.isOK()){
            Log.info("User Photo was Successfully updated: " + name);
        } else {
            Log.info("User photo was not successfully updated " + result.error());
        }
    }
}
