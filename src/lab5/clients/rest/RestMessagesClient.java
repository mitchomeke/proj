package lab5.clients.rest;

import lab5.api.Message;
import lab5.api.java.Messages;
import lab5.api.java.Result;
import lab5.clients.grpc.GrpcUsersClient;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class RestMessagesClient extends RestClient implements Messages {
    private static Logger Log = Logger.getLogger(RestMessagesClient.class.getName());
    public RestMessagesClient(URI serverURI) {
        super(serverURI, Log);
    }

    @Override
    public Result<String> postMessage(String pwd, Message msg) {
        return null;
    }

    @Override
    public Result<Message> getInboxMessage(String name, String mid, String pwd) {
        return null;
    }

    @Override
    public Result<List<String>> getAllInboxMessages(String name, String pwd) {
        return null;
    }

    @Override
    public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
        return null;
    }

    @Override
    public Result<Void> deleteMessage(String name, String mid, String pwd) {
        return null;
    }

    @Override
    public Result<List<String>> searchInbox(String name, String pwd, String query) {
        return null;
    }
}
