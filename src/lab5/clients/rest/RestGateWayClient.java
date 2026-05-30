package lab5.clients.rest;

import lab5.api.Message;
import lab5.api.User;
import lab5.api.java.Result;

import java.net.URI;
import java.util.logging.Logger;

public class RestGateWayClient extends RestClient{
    private static Logger Log = Logger.getLogger(RestGateWayClient.class.getName());
    private final RestUsersClient usersClient;
    private final RestMessagesClient messagesClient;
    public RestGateWayClient(URI serverURI) {
        super(serverURI,Log);
        this.usersClient = new RestUsersClient(serverURI);
        this.messagesClient = new RestMessagesClient(serverURI);
    }
    public Result<User> getUser(String userName, String password){
        return usersClient.getUser(userName,password);
    }
    public Result<String> sendMessage(String password, Message msg){
        return messagesClient.postMessage(password,msg);
    }
}
