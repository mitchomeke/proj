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
import lab5.server.java.JavaUsers;
import lab5.server.persistence.Hibernate;

@Singleton
public class RestUsersResource extends RestResource implements RestUsers {

	private static Logger Log = Logger.getLogger(RestUsersResource.class.getName());

	final Users impl;

	public RestUsersResource() {
		impl = new JavaUsers();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String postUser(User user) {
		Log.info("postUser : " + user);
		// Check if user data is valid
		Result<String> result = impl.postUser(user);
		if (result.isOK()){
			return result.value();
		} else {
			throw new WebApplicationException(unwrapResultOrThrow(result));
		}
	}


	@GET
	@Path("/{" + NAME +"}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public User getUser(@PathParam(NAME) String name, @QueryParam(PWD) String pwd) {
		Log.info("getUser : user = " + name + "; pwd = " + pwd);
		Result<User> result = impl.getUser(name,pwd);
		if (result.isOK()){
			return result.value();
		} else {
			throw new WebApplicationException(errorCodeToStatus(result.error()));
		}
	}

	@PUT
	@Path("/{" + NAME +"}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public User updateUser(@PathParam(NAME) String name, @QueryParam(PWD) String pwd, User user) {
		Log.info("updateUser : user = " + name + "; pwd = " + pwd + " ; userData = " + user);
		Result<User> result = impl.updateUser(name,pwd,user);
		if (result.isOK()){
			return result.value();
		} else {
			throw new WebApplicationException(result.error().name());
		}
	}

	@DELETE
	@Path("/{" + NAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public User deleteUser(@PathParam(NAME) String name, @QueryParam(PWD) String pwd) {
		Log.info("deleteUser : user = " + name + "; pwd = " + pwd);
		Result<User> result = impl.deleteUser(name,pwd);
		if (result.isOK()){
			return result.value();
		} else {
			throw new WebApplicationException(result.error().name());
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public List<User> searchUsers(@QueryParam(QUERY) String pattern) {
		Log.info("searchUsers : pattern = " + pattern);
		Result<List<User>> result = impl.searchUsers(pattern);
		if (result.isOK()){
			return result.value();
		} else {
			throw new WebApplicationException(result.error().name());
		}
	}

	@GET
	@Path("/{" + NAME + "}/" + PHOTO)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Override
	public byte[] getUserPhoto(@PathParam(NAME) String name, @QueryParam(PWD) String pwd) {
		Log.info("getUserPhoto : user = " + name);
		Result<byte[]> result = impl.getUserPhoto(name, pwd);
		if (result.isOK()){
			return result.value();
		} else {
			throw new WebApplicationException(result.error().name());
		}
	}

	@PUT
	@Path("/{" + NAME + "}/" + PHOTO)
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public User updateUserPhoto(@PathParam(NAME) String name, @QueryParam(PWD) String pwd, byte[] photo) {
		Log.info("updateUserPhoto : user = " + name);
		Result<User> result = impl.updateUserPhoto(name,pwd,photo);
		if (result.isOK()){
			return result.value();
		} else {
			throw new WebApplicationException(result.error().name());
		}
	}
}
