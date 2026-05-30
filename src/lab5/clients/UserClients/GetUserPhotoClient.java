package lab5.clients.UserClients;

import lab5.api.java.Result;
import lab5.api.java.Users;
import lab5.clients.java.Clients;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class GetUserPhotoClient {
    private static Logger Log = Logger.getLogger(GetUserPhotoClient.class.getName());

    public static void main (String[] args) throws IOException {
        if (args.length != 4){
            System.err.println("Use java: "+ GetUserPhotoClient.class.getCanonicalName() + "name pwd file domain");
            return;
        }
        String name = args[0];
        String pwd = args[1];
        String fileName = args[2];
        String domain = args[3];

        Path photoFilePath = Paths.get(fileName);
        if (!Files.exists(photoFilePath)){
            System.err.println("File :" + fileName + " does not exist");
            System.exit(1);
        }

        byte[] photoData = Files.readAllBytes(photoFilePath);

        if (photoData.length == 0){
            System.err.println("File: " +fileName+ " has no data (empty file)");
            System.exit(1);
        }

        Users client = Clients.UsersClient.get(domain);
        Result<byte[]> result = client.getUserPhoto(name,pwd);

        if (result.isOK()){
            Log.info("Photo was successfully gotten "+ fileName);
        } else {
            Log.info("Unable to get Photo" + fileName);
        }
    }
}
