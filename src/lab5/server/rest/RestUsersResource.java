package lab5.server.rest;

import java.util.List;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lab5.api.User;
import lab5.api.java.Result;
import lab5.api.java.Users;
import lab5.api.rest.RestUsers;
import lab5.clients.UserClients.GetUserClient;
import lab5.server.java.JavaUsers;
import lab5.server.persistence.Hibernate;

@Singleton
public class RestUsersResource extends RestResource implements RestUsers {

	private static Logger Log = Logger.getLogger(RestUsersResource.class.getName());

	final Users impl;

	public RestUsersResource() {
		impl = new JavaUsers();
	}


	@Override
	public String postUser(User user) {
	//	Log.info("postUser : " + user);
		// Check if user data is valid
		Result<String> result = impl.postUser(user);
		if (result.isOK()){
			return result.value();
		} else {
			throw new WebApplicationException(unwrapResultOrThrow(result));
		}
	}



	@Override
	public User getUser( String name,  String pwd) {
	//	Log.info("getUser : user = " + name + "; pwd = " + pwd);
		Result<User> result = impl.getUser(name,pwd);
		if (result.isOK()){
			return result.value();
		} else {
			Log.info("Get User -> Error:"+result.error().name());
			throw new WebApplicationException(errorCodeToStatus(result.error()));
		}
	}


	@Override
	public User updateUser( String name,String pwd, User user) {
		Log.info("updateUser : user = " + name + "; pwd = " + pwd + " ; userData = " + user);
		Result<User> result = impl.updateUser(name,pwd,user);
		if (result.isOK()){
			return result.value();
		} else {
			throw new WebApplicationException(result.error().name());
		}
	}


	@Override
	public User deleteUser( String name,  String pwd) {
		Log.info("deleteUser : user = " + name + "; pwd = " + pwd);
		Result<User> result = impl.deleteUser(name,pwd);
		if (result.isOK()){
			return result.value();
		} else {
			throw new WebApplicationException(result.error().name());
		}
	}

/*
	@Override
	public List<User> searchUsers( String pattern) {
		Log.info("searchUsers : pattern = " + pattern);
		Result<List<User>> result = impl.searchUsers(pattern);
		if (result.isOK()){
			return result.value();
		} else {
			throw new WebApplicationException(result.error().name());
		}
	}
 */



	@Override
	public byte[] getUserPhoto( String name,  String pwd) {
		Log.info("getUserPhoto : user = " + name);
		Result<byte[]> result = impl.getUserPhoto(name, pwd);
		if (result.isOK()){
			return result.value();
		} else {
			throw new WebApplicationException(result.error().name());
		}
	}


	@Override
	public User updateUserPhoto(String name,  String pwd, byte[] photo) {
		Log.info("updateUserPhoto : user = " + name);
		Result<User> result = impl.updateUserPhoto(name,pwd,photo);
		if (result.isOK()){
			return result.value();
		} else {
			throw new WebApplicationException(result.error().name());
		}
	}
}
