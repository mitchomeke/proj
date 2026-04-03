package lab5.clients.grpc;

import lab5.api.Message;
import lab5.api.java.Messages;
import lab5.api.java.Result;
import org.hsqldb.persist.Log;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class GrpcMessagesClient extends GrpcClient implements Messages {
    private static Logger Log = Logger.getLogger(GrpcMessagesClient.class.getName());
    protected GrpcMessagesClient(URI serverURI) {
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
