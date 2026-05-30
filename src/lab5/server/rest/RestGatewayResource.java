package lab5.server.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lab5.Discovery;
import lab5.api.Message;
import lab5.api.User;
import lab5.api.java.Messages;
import lab5.api.java.Result;
import lab5.api.java.Users;
import lab5.api.rest.RestMessages;
import lab5.api.rest.RestUsers;
import lab5.clients.rest.RestMessagesClient;
import lab5.clients.rest.RestUsersClient;

import java.util.logging.Logger;

public class RestGatewayResource extends RestResource{
    private static Logger Log = Logger.getLogger(RestGatewayResource.class.getName());

    /*
        private final RestUsersClient usersClient;
    private final RestMessagesClient messagesClient;
     */


    public RestGatewayResource(){
        /*
                Discovery discovery = Discovery.getInstance();
        usersClient = new RestUsersClient(discovery.knownUrisOf(Users.SERVICE_NAME,1)[0]);
        messagesClient = new RestMessagesClient(discovery.knownUrisOf(Messages.SERVICE_NAME,1)[0]);
         */

    }
/*
    public User getUser(String name, String pwd) {
        Log.info("Gateway: getUser -> " + name);
        try {
            // The Gateway acts as a middleman
            return usersClient.getUser(name,pwd).value();
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.BAD_GATEWAY);
        }

}
 */

}
