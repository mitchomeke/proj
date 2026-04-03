package lab5.server.java;

import java.util.List;
import java.util.logging.Logger;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lab5.api.User;
import lab5.api.java.Result;
import lab5.api.java.Result.ErrorCode;
import lab5.api.java.Users;
import lab5.server.persistence.Hibernate;
import org.hibernate.StaleObjectStateException;
import org.hibernate.TransientObjectException;

public class JavaUsers implements Users {

	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	private Hibernate hibernate;

	public JavaUsers() {
		hibernate = Hibernate.getInstance();
	}

	@Override
	public Result<String> postUser(User user) {
		Log.info("createUser : " + user);

		// Check if user data is valid
		if (user.getName() == null || user.getPwd() == null || user.getDisplayName() == null
				|| user.getDomain() == null) {
			Log.info("User object invalid.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		try {
			hibernate.persist(user);
		} catch (Exception e) {
			e.printStackTrace(); //Most likely the exception is due to the user already existing...
			Log.info("User already exists.");
			return Result.error(ErrorCode.CONFLICT);
		}

		return Result.ok(user.getName());
	}

	@Override
	public Result<User> getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);

		// Check if user is valid
		if (userId == null || password == null) {
			Log.info("UserId or password null.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		User user = null;
		try {
			user = hibernate.get(User.class, userId);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}

		// Check if user exists and password is correct...
		if (user == null || !user.getPwd().equals(password)) {
			Log.info("Password is incorrect");
			return Result.error(ErrorCode.FORBIDDEN);
		}

		return Result.ok(user);

	}

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		Log.info("updateUser : name = " + userId + "; pwd = " + password + " ; info = " + user);
		if (userId == null || password == null) {
			Log.info("Name or Password is null.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		if (user.getName() == null || user.getPwd() == null || user.getDomain() == null || user.getDisplayName() == null) {
			Log.info("Some Info Credentials may be null");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		User existingUser = getUser(userId,password).value();
		if (existingUser == null){
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
		if (!existingUser.getPwd().equals(password)){
			return Result.error(ErrorCode.CONFLICT);
		}
		existingUser.setDisplayName(user.getDisplayName());
		existingUser.setPwd(user.getPwd());
		existingUser.setDomain(user.getDomain());
		existingUser.setPhoto(user.getPhoto());
		existingUser.setPhoneNumbers(user.getPhoneNumbers());
		try {
			hibernate.update(existingUser);
			return Result.ok(existingUser);
		} catch (StaleObjectStateException s) {
			Log.info("Data changed while updating");
			s.printStackTrace();
			return Result.error(ErrorCode.CONFLICT);
		} catch (TransientObjectException t) {
			Log.info("Object does not exist in the database");
			return Result.error(ErrorCode.NOT_FOUND);
		} catch (ConstraintViolationException c) {
			Log.info("Object does not meet constraints of the database");
			return Result.error(ErrorCode.CONFLICT);
		} catch (Exception x) {
			x.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
		Log.info("deleteUser : name = " + userId + "; pwd = " + password);
		if (userId == null || password == null) {
			Log.info("Name or password null.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		User var;
		if ((var = getUser(userId,password).value()) != null){
			try {
				hibernate.delete(var);
			} catch (ConstraintViolationException c){
				c.printStackTrace();
				Log.info("Variable does not meet the database constraints");
				return Result.error(ErrorCode.CONFLICT);
			} catch (TransientObjectException t){
				t.printStackTrace();
				Log.info("User does not Exist in the database");
			} catch (Exception x){
				x.printStackTrace();
				return Result.error(ErrorCode.INTERNAL_ERROR);
			}
			return Result.ok(var);
		} else {
			return Result.error(ErrorCode.FORBIDDEN);
		}
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		Log.info("pattern = " + pattern);
		if (pattern == null){
			Log.info("Pattern is null");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
			try {
				List<User> list = hibernate.jpql("SELECT u FROM User u WHERE u.name LIKE '%" + pattern +
						"%'",User.class);
				return Result.ok(list);
			} catch (Exception e){
				e.printStackTrace();
				return Result.error(ErrorCode.INTERNAL_ERROR);
			}
	}

	@Override
	public Result<byte[]> getUserPhoto(String name, String pwd) {
		Log.info("getUserPhoto : user = " + name + "; pwd = " + pwd);
		return Result.ok(getUser(name,pwd).value().getPhoto());
	}

	@Override
	public Result<User> updateUserPhoto(String name, String pwd, byte[] photo) {
		Log.info("updateUserPhoto : user = " + name + "; pwd = " + pwd);

		// Check if user is valid
		if (name == null || pwd == null || photo == null) {
			Log.info("UserId or password or photo null.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		User user = null;
		try {
			user = getUser(name,pwd).value();
			user.setPhoto(photo);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
		return Result.ok(user);
	}

}
