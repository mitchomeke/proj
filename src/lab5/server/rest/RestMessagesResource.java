package lab5.server.rest;

import jakarta.ws.rs.WebApplicationException;
import lab5.api.Message;
import lab5.api.java.Messages;
import lab5.api.java.Result;
import lab5.api.java.Users;
import lab5.server.java.JavaMessages;

import java.util.List;
import java.util.logging.Logger;

public class RestMessagesResource extends RestResource implements Messages {

    private static Logger Log = Logger.getLogger(RestUsersResource.class.getName());

    final Messages impl;
    public RestMessagesResource(){
        impl = new JavaMessages();
    }
    @Override
    public Result<String> postMessage(String pwd, Message msg) {
        Log.info("postMessage: " + msg.getSender());
        Result<String> result = impl.postMessage(pwd,msg);
        if(!result.isOK()){
            throw new WebApplicationException(unwrapResultOrThrow(result));
        } else {
            return result;
        }
    }

    @Override
    public Result<Message> getInboxMessage(String name, String mid, String pwd) {
        Log.info("getInboxMessage: " + name);
        Result<Message> result = impl.getInboxMessage(name,mid,pwd);
        if (result.isOK()){
            return result;
        } else {
            throw new WebApplicationException(unwrapResultOrThrow(result).toString());
        }
    }

    @Override
    public Result<List<String>> getAllInboxMessages(String name, String pwd) {
        Log.info("getAllInboxMessages: " + name);
        Result<List<String>> result = impl.getAllInboxMessages(name, pwd);
        if (result.isOK()){
            return result;
        } else {
            throw new WebApplicationException(unwrapResultOrThrow(result).toString());
        }
    }

    @Override
    public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
        Log.info("removeInboxMessage: " + name);
        Result<Void> result = impl.removeInboxMessage(name,mid,pwd);
        if (result.isOK()){
            return Result.ok(null);
        } else {
            throw new WebApplicationException(unwrapResultOrThrow(result).toString());
        }
    }

    @Override
    public Result<Void> deleteMessage(String name, String mid, String pwd) {
        Log.info("deleteMessage: " + name);
        Result<Void> result = impl.deleteMessage(name,mid,pwd);
        if (result.isOK()){
            return Result.ok(null);
        } else {
            throw new WebApplicationException(unwrapResultOrThrow(result).toString());
        }
    }

    @Override
    public Result<List<String>> searchInbox(String name, String pwd, String query) {
        Log.info("searchInbox: " + name);
        Result<List<String>> result = impl.searchInbox(name,pwd,query);
        if (result.isOK()){
            return result;
        } else {
            throw new WebApplicationException(unwrapResultOrThrow(result).toString());
        }
    }
}
