package lab5.server.java;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lab5.Discovery;
import lab5.api.Message;
import lab5.api.User;
import lab5.api.java.Messages;
import lab5.api.java.Result;
import lab5.clients.rest.RestMessagesClient;
import lab5.clients.rest.RestUsersClient;
import lab5.server.persistence.Hibernate;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

public class JavaMessages implements Messages {
    private static Logger log = Logger.getLogger(JavaMessages.class.getName());
    private final Hibernate hibernate;
    private final String myDomain;
    private final Discovery discovery;
    private static final String SECRT = "SECRET";
    private final RestUsersClient users;


    public JavaMessages(){
        hibernate = Hibernate.getInstance();
        myDomain = System.getProperty("mydomain","defaultDomain");
        discovery = Discovery.getInstance();
        URI userServerUri = discovery.lookup(myDomain,"users");
        users = new RestUsersClient(userServerUri);
    }
    @Override
    public Result<String> postMessage(String pwd, Message msg) {
        log.info("postMessage: " + msg);

        if (msg.getSubject() == null || msg.getContents() == null || msg.getId() == null
                || msg.getSender() == null || msg.getDestination() == null || pwd == null) {
            log.info("Message object invalid or Password is Null");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        Result<User> result = users.getUser(msg.getSender(),pwd);
        User sender = result.value();
        if (result.isOK()){
            String generatedId = UUID.randomUUID().toString();
            msg.setId(generatedId);
            msg.setCreationTime(System.currentTimeMillis());
            Set<String> recipients = msg.getDestination();
            Set<String> localNames = new HashSet<>();
            Set<String> remoteDomain = new HashSet<>();
            String formatSender = String.format("%s <%s@%s>",sender.getDisplayName(),sender.getName(),sender.getDomain());
            msg.setSender(formatSender);
            for (String name : recipients){
                if (name.contains("@")){
                    String[] splitName = name.split("@");
                    String userPart = splitName[0];
                    String domainPart = splitName[1];

                    if (domainPart.equals(myDomain)){
                        localNames.add(userPart);
                    } else {
                        remoteDomain.add(domainPart);
                    }
                } else {
                    localNames.add(name);
                }
            }
            for(String name : localNames){
                if (!users.getUser(name,null).isOK()){
                   log.info("Recipient" + name + "does not exist");
                   return Result.error(Result.ErrorCode.NOT_FOUND);
                }
            }
            hibernate.persist(msg);
            if (!remoteDomain.isEmpty()){
                propagateToRemote(msg,remoteDomain);
            }
        } else {
            log.info("Sender "+ msg.getSender() + " does not exist");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        return Result.ok(msg.getId());
    }

    private void propagateToRemote(Message msg, Set<String> remoteDomain) {
        new Thread(() -> {
            try {
                for (String domain : remoteDomain){
                    URI serviceURI = discovery.lookup(domain, Messages.SERVICE_NAME);
                    var messagesClient = new RestMessagesClient(serviceURI);
                    messagesClient.postMessage(SECRT,msg);
                }
            }catch (Exception x){
                x.printStackTrace();
            }
        }).start();
    }

    @Override
    public Result<Message> getInboxMessage(String name, String mid, String pwd) {
        log.info("getSender : name = " + name + "; pwd = " + pwd + "message id : mid = " + mid);
        if (name == null || pwd == null || mid == null){
            log.info("Name or Password or Message Id null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        Result<User> result = users.getUser(name,pwd);
        if (!result.isOK()){
            log.info("User not found " + name);
            return Result.error(result.error());
        } else {
            User user = result.value();
            String Domain  = user.getDomain();
            if(myDomain.equals(Domain)){
                Message msg = hibernate.get(Message.class,mid);
                if (msg == null){
                    log.info("Message not found");
                    return Result.error(Result.ErrorCode.NOT_FOUND);
                }
                if (msg.getDestination().contains(name) || msg.getDestination().contains(name + "@"+ myDomain)){
                    return Result.ok(msg);

                } else {
                    return Result.error(Result.ErrorCode.CONFLICT);
                }
            } else {
                URI serviceUri = discovery.lookup(Domain,Messages.SERVICE_NAME);
                var messagesClient = new RestMessagesClient(serviceUri);
                Result<Message> messageResult = messagesClient.getInboxMessage(name, mid, pwd);
                if (messageResult.isOK()){
                    return messageResult;
                } else {
                    log.info("Message not found"+ mid);
                    return Result.error(messageResult.error());
                }
            }
        }
    }

    @Override
    public Result<List<String>> getAllInboxMessages(String name, String pwd) {
        log.info("getUserName : name = " + name + "; pwd = " + pwd);
        if (name == null || pwd == null){
            log.info("Name or Password.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        Result<User> userResult = users.getUser(name,pwd);
        if (!userResult.isOK()){
            return Result.error(userResult.error());
        } else {
            User user = userResult.value();
            String domain = user.getDomain();
            if (domain.equals(myDomain)){
                String jpql = "SELECT m FROM Message m";
                List<Message> messages = hibernate.jpql(jpql,Message.class);
                return getStrings(name,messages,domain);
            } else {
                URI serverUri = discovery.lookup(domain,Messages.SERVICE_NAME);
                var client = new RestMessagesClient(serverUri);
                Result<List<String>> resultList = client.getAllInboxMessages(name,pwd);
                if (!resultList.isOK()){
                    return Result.error(resultList.error());
                } else {
                    return resultList;
                }
            }
        }
    }

    private Result<List<String>> getStrings(String name, List<Message> messages, String domain) {
        List<String> messageIds = new ArrayList<>();
        if (messages != null){
            for (Message msg : messages){
                if (msg.getDestination().contains(name) || msg.getDestination().contains(name +"@"+ domain)){
                    messageIds.add(msg.getId());
                }
            }
        }
        return Result.ok(messageIds);
    }

    @Override
    public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
        log.info("getUserName : name = " + name + "; pwd = " + pwd + "message id : id = " + mid);
        if (name == null || pwd == null || mid == null){
            log.info("Name or Password or Message id null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        Result<User> userResult = users.getUser(name,pwd);
        if (!userResult.isOK()){
            return Result.error(userResult.error());
        }
        else {
            User user = userResult.value();
            String domain = user.getDomain();
            if (domain.equals(myDomain)){
                Message msg = hibernate.get(Message.class,mid);
                if (msg == null){
                    log.info("Message does not exist "+mid);
                    return Result.error(Result.ErrorCode.NOT_FOUND);
                }
                if (msg.getDestination().contains(name) || msg.getDestination().contains(name+"@"+domain)){
                    if(msg.getDestination().remove(name) || msg.getDestination().remove(name+"@"+domain)){
                        hibernate.update(msg);
                    }
                } else {
                    return Result.error(Result.ErrorCode.CONFLICT);
                }
            } else {
                URI serverURI = discovery.lookup(domain,Messages.SERVICE_NAME);
                var client = new RestMessagesClient(serverURI);
                Result<Void> res = client.removeInboxMessage(name,mid,pwd);
                if (!res.isOK()){
                    return Result.error(res.error());
                }
            }
        }
        return Result.ok();
    }

    @Override
    public Result<Void> deleteMessage(String name, String mid, String pwd) {
        log.info("getSenderName : name = " + name + "; pwd = " + pwd + "message id : id = " + mid);
        if (name == null || pwd == null || mid == null){
            log.info("Name or Password or Message id null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        Result<User> userResult = users.getUser(name,pwd);
        if (!userResult.isOK()){
            return Result.error(userResult.error());
        } else {
            User user = userResult.value();
            String domain = user.getDomain();
            if (domain.equals(myDomain)){
                Message msg = hibernate.get(Message.class,mid);
                if (msg == null){
                    log.info("Message Not Found");
                    return Result.error(Result.ErrorCode.NOT_FOUND);
                }
                if (msg.getSender().equals(name) || msg.getSender().equals(name+"@"+domain)){
                    Long now = System.currentTimeMillis();
                    Long messageTime = msg.getCreationTime();
                    if (now-messageTime <= 30000){
                        hibernate.delete(msg);
                    } else {
                        log.info("Too late to delete");
                        return Result.error(Result.ErrorCode.TIMEOUT);
                    }
                }
            }
            URI serverUri = discovery.lookup(domain,Messages.SERVICE_NAME);
            var client = new RestMessagesClient(serverUri);
            Result<Void> res = client.deleteMessage(name,mid,pwd);
            if (!res.isOK()){
                return Result.error(Result.ErrorCode.CONFLICT);
            }
        }
        return Result.ok();
    }

    @Override
    public Result<List<String>> searchInbox(String name, String pwd, String query) {
        log.info("getUserName : name = " + name + "; pwd = " + pwd + "message query : query = " + query);
        if (name == null || pwd == null || query == null){
            log.info("Name or Password or Query null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        Result<User> userResult = users.getUser(name,pwd);
        if (!userResult.isOK()){
            return Result.error(userResult.error());
        } else {
            User user = userResult.value();
            String domain = user.getDomain();
            if (domain.equals(myDomain)){
                String queryString = query.toLowerCase();
                String jpql = "SELECT m FROM Message m WHERE (LOWER(m.subject) LIKE '%" + queryString + "%' " +
                        "OR LOWER(m.contents) LIKE '%" + queryString + "%')";
                List<Message> messages = hibernate.jpql(jpql,Message.class);
                return getStrings(name,messages,domain);
            } else {
                URI serverUri = discovery.lookup(domain,Messages.SERVICE_NAME);
                var client = new RestMessagesClient(serverUri);
                Result<List<String>> resultList = client.searchInbox(name,pwd,query);
                if (!resultList.isOK()){
                    return Result.error(resultList.error());
                } else {
                    return resultList;
                }
            }
        }
    }
}
