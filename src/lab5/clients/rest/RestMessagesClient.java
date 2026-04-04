package lab5.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lab5.api.Message;
import lab5.api.User;
import lab5.api.java.Messages;
import lab5.api.java.Result;
import lab5.api.rest.RestMessages;
import lab5.clients.grpc.GrpcUsersClient;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class RestMessagesClient extends RestClient implements Messages {
    private static Logger Log = Logger.getLogger(RestMessagesClient.class.getName());
    public RestMessagesClient(URI serverURI) {
        super(serverURI, Log);
        target = super.target.path(RestMessages.PATH);
    }

    @Override
    public Result<String> postMessage(String pwd, Message msg) {
        return super.reTry(() -> doPostMessage(pwd,msg));
    }

    private Result<String> doPostMessage(String pwd, Message msg) {
        Response r = target.path(msg.getSender())
                .queryParam(RestMessages.PWD,pwd)
                .request().accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(msg,MediaType.APPLICATION_JSON));
        return super.processResponse(r,String.class);
    }

    @Override
    public Result<Message> getInboxMessage(String name, String mid, String pwd) {
        Response r = target.path(RestMessages.MBOX).path(name).path(mid)
                .queryParam(RestMessages.PWD,pwd).request().
                accept(MediaType.APPLICATION_JSON).get();
        return super.processResponse(r,Message.class);
    }

    @Override
    public Result<List<String>> getAllInboxMessages(String name, String pwd) {
        Response r = target.path(RestMessages.MBOX).path(name)
                .queryParam(RestMessages.PWD,pwd).request().
                accept(MediaType.APPLICATION_JSON).get();
        return super.processResponse(r,new GenericType<List<String>>(){});
    }

    @Override
    public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
        Response r = target.path(RestMessages.MBOX).
                path(name).path(mid).queryParam(RestMessages.PWD,pwd).
                request().accept(MediaType.APPLICATION_JSON).delete();
        return super.processResponse(r);
    }

    @Override
    public Result<Void> deleteMessage(String name, String mid, String pwd) {
        Response r = target.path(name).
                path(mid).queryParam(RestMessages.PWD,pwd).
                request().accept(MediaType.APPLICATION_JSON).delete();
        return super.processResponse(r);
    }

    @Override
    public Result<List<String>> searchInbox(String name, String pwd, String query) {
        Response r = target.path(RestMessages.MBOX).
                path(name).queryParam(RestMessages.PWD,pwd).
                queryParam(RestMessages.QUERY,query).request().
                accept(MediaType.APPLICATION_JSON).get();
        return super.processResponse(r,new GenericType<List<String>>(){});
    }
}
