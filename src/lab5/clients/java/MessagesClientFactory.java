package lab5.clients.java;

import lab5.Discovery;
import lab5.api.Message;
import lab5.api.User;
import lab5.api.java.Messages;
import lab5.api.java.Result;
import lab5.api.java.Users;
import lab5.clients.grpc.GrpcMessagesClient;
import lab5.clients.grpc.GrpcUsersClient;
import lab5.clients.rest.RestMessagesClient;
import lab5.clients.rest.RestUsersClient;

import java.net.URI;

public class MessagesClientFactory {

    private static final String REST = "/rest";
    private static final String GRPC = "/grpc";
    private static final Object DOMAIN_DELIMITER = "@";

    static public Messages get(String domain) {
        var sn = "%s%s%s".formatted(Messages.SERVICE_NAME, DOMAIN_DELIMITER, domain);
        return newClient(Discovery.knownUrisOf(sn,1)[0]);
    }

    static private Messages newClient( URI serverURI ) {
        var path = serverURI.getPath();
        if (path.endsWith(REST))
            return new RestMessagesClient(serverURI);
        if (path.endsWith(GRPC))
            return new GrpcMessagesClient(serverURI);
        throw new RuntimeException("Unknown service type..." + serverURI);
    }


    public static void main(String[] args ) {

        Messages client = MessagesClientFactory.get("some_domain");

        Result<Message> m = client.getInboxMessage("name","mid","password");

        if(m.isOK())
            System.out.println("Got message: " + m.value() );
        else
            System.out.println("Got error:" + m.error() );
    }
}