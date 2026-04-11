package lab5.server.rest;

import jakarta.ws.rs.WebApplicationException;
import lab5.api.Message;
import lab5.api.java.Messages;
import lab5.api.java.Result;
import lab5.api.java.Users;
import lab5.api.rest.RestMessages;
import lab5.server.java.JavaMessages;

import java.util.List;
import java.util.logging.Logger;

public class RestMessagesResource extends RestResource implements RestMessages {

    private static Logger Log = Logger.getLogger(RestMessagesResource.class.getName());

    final Messages impl;
    public RestMessagesResource(){
        impl = new JavaMessages();
    }
    @Override
    public String postMessage(String pwd, Message msg) {
        Log.info("postMessage: " + msg.getSender());
        Result<String> result = impl.postMessage(pwd,msg);
        if(!result.isOK()){
            throw new WebApplicationException(unwrapResultOrThrow(result));
        } else {
            return result.value();
        }
    }

    @Override
    public Message getMessage(String name, String mid, String pwd) {
        Log.info("getInboxMessage: " + name);
        Result<Message> result = impl.getInboxMessage(name,mid,pwd);
        if (result.isOK()){
            return result.value();
        } else {
            throw new WebApplicationException(unwrapResultOrThrow(result).toString());
        }
    }

    @Override
    public List<String> getMessages(String name, String pwd, String query) {
        Log.info("getAllInboxMessages: " + name);
        Result<List<String>> result = impl.getAllInboxMessages(name, pwd);
        if (result.isOK()){
            return result.value();
        } else {
            throw new WebApplicationException(unwrapResultOrThrow(result).toString());
        }
    }

    @Override
    public void removeFromUserInbox(String name, String mid, String pwd) {
        Log.info("removeInboxMessage: " + name);
        Result<Void> result = impl.removeInboxMessage(name,mid,pwd);
        if (result.isOK()){
        } else {
            throw new WebApplicationException(unwrapResultOrThrow(result).toString());
        }
    }

    @Override
    public void deleteMessage(String name, String mid, String pwd) {
        Log.info("deleteMessage: " + name);
        Result<Void> result = impl.deleteMessage(name,mid,pwd);
        if (result.isOK()){
        } else {
            throw new WebApplicationException(unwrapResultOrThrow(result).toString());
        }
    }
}
