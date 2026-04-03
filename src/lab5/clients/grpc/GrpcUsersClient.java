package lab5.clients.grpc;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;
import lab5.api.User;
import lab5.api.java.Result;
import lab5.api.java.Result.ErrorCode;
import lab5.api.java.Users;
import lab5.server.grpc.generated_java.GrpcUsersGrpc;
import lab5.server.grpc.generated_java.UsersProtoBuf;
import lab5.server.grpc.generated_java.UsersProtoBuf.GetUserArgs;
import lab5.server.grpc.generated_java.UsersProtoBuf.GetUserResult;
import lab5.server.util.DataModelAdaptor;

public class GrpcUsersClient extends GrpcClient implements Users {

	private static Logger Log = Logger.getLogger(GrpcUsersClient.class.getName());
	

	final GrpcUsersGrpc.GrpcUsersBlockingStub stub;

	public GrpcUsersClient(URI serverURI) {
		super( serverURI, Log );
		
		stub = GrpcUsersGrpc.newBlockingStub( channel );
	}

	@Override
	public Result<String> postUser(User user) {
		return super.processResponse( () -> stub.postUser(DataModelAdaptor.User_to_GrpcUser(user)).getUserAddress() );
	}

	@Override
	public Result<User> getUser(String userId, String password) {
		GetUserResult res = stub.getUser(GetUserArgs.newBuilder()
					.setName(userId).setPwd(password)
					.build());

		return super.processResponse( () -> DataModelAdaptor.GrpcUser_to_User(res.getUser()) );
	}

	@Override
	public Result<User> updateUser(String userId, String pwd, User user) {
	UsersProtoBuf.UpdateUserResult result = stub.updateUser(UsersProtoBuf.
			UpdateUserArgs.newBuilder().setName(userId).
				setPwd(pwd).setInfo(DataModelAdaptor.User_to_GrpcUser(user)).build());

	return super.processResponse(() -> DataModelAdaptor.GrpcUser_to_User(result.getUser()));
	}

	@Override
	public Result<User> deleteUser(String userId, String pwd) {
		UsersProtoBuf.DeleteUserResult result = stub.deleteUser(UsersProtoBuf.DeleteUserArgs.newBuilder()
				.setName(userId).setPwd(pwd).build());
		return super.processResponse(() -> DataModelAdaptor.GrpcUser_to_User(result.getUser()));
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		return super.processResponse(() -> (List<User>) stub.searchUsers(UsersProtoBuf.SearchUsersArgs.newBuilder().setQuery(pattern).build())) ;
	}

	@Override
	public Result<byte[]> getUserPhoto(String name, String pwd) {
		return super.processResponse(() -> stub.getUserPhoto(UsersProtoBuf.GetUserPhotoArgs.newBuilder().setName(name).setPwd(pwd).build()).toByteArray());
	}

	@Override
	public Result<User> updateUserPhoto(String name, String pwd, byte[] photo) {
		UsersProtoBuf.UpdateUserPhotoResult result = stub.updateUserPhoto(UsersProtoBuf.UpdateUserPhotoArgs.newBuilder().setName(name).setPwd(pwd).setPhoto(ByteString.copyFrom(photo)).build());
		return super.processResponse(() -> DataModelAdaptor.GrpcUser_to_User(result.getUser()));
	}

	
}
