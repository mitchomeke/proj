package lab5.server.grpc;

import static lab5.server.util.DataModelAdaptor.GrpcUser_to_User;
import static lab5.server.util.DataModelAdaptor.User_to_GrpcUser;

import com.google.protobuf.ByteString;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import lab5.api.User;
import lab5.api.java.Result;
import lab5.api.java.Result.ErrorCode;
import lab5.api.java.Users;
import lab5.server.grpc.generated_java.GrpcUsersGrpc;
import lab5.server.grpc.generated_java.UsersProtoBuf.DeleteUserArgs;
import lab5.server.grpc.generated_java.UsersProtoBuf.DeleteUserResult;
import lab5.server.grpc.generated_java.UsersProtoBuf.GetUserArgs;
import lab5.server.grpc.generated_java.UsersProtoBuf.GetUserPhotoArgs;
import lab5.server.grpc.generated_java.UsersProtoBuf.GetUserPhotoResult;
import lab5.server.grpc.generated_java.UsersProtoBuf.GetUserResult;
import lab5.server.grpc.generated_java.UsersProtoBuf.GrpcUser;
import lab5.server.grpc.generated_java.UsersProtoBuf.PostUserResult;
import lab5.server.grpc.generated_java.UsersProtoBuf.SearchUsersArgs;
import lab5.server.grpc.generated_java.UsersProtoBuf.UpdateUserArgs;
import lab5.server.grpc.generated_java.UsersProtoBuf.UpdateUserPhotoArgs;
import lab5.server.grpc.generated_java.UsersProtoBuf.UpdateUserPhotoResult;
import lab5.server.grpc.generated_java.UsersProtoBuf.UpdateUserResult;
import lab5.server.java.JavaUsers;
import lab5.server.util.DataModelAdaptor;

import java.util.List;

public class GrpcUsersController extends GrpcController implements GrpcUsersGrpc.AsyncService, BindableService {

	Users impl = new JavaUsers();

	@Override
	public final ServerServiceDefinition bindService() {
		return GrpcUsersGrpc.bindService(this);
	}

	public void postUser(GrpcUser user, StreamObserver<PostUserResult> responseObserver) {
		Result<String> result = impl.postUser(DataModelAdaptor.GrpcUser_to_User(user));
		if (!result.isOK()){
			responseObserver.onError(errorCodeToStatus(result.error()));
		} else {
			User user1 = DataModelAdaptor.GrpcUser_to_User(user);
			try {
				responseObserver.onNext(PostUserResult.newBuilder().setUserAddress(user1.getName()+"@"+user1.getDomain()).build());
				responseObserver.onCompleted();
			} catch (Exception e){
				responseObserver.onError(errorCodeToStatus(ErrorCode.INTERNAL_ERROR));
			}
		}
	}

	@Override
	public void getUser(GetUserArgs request, StreamObserver<GetUserResult> responseObserver) {
		Result<User> result = impl.getUser(request.getName(),request.getPwd());
		if (!result.isOK()){
			responseObserver.onError(errorCodeToStatus(result.error()));
		} else {
			try {
				responseObserver.onNext(GetUserResult.newBuilder().setUser(DataModelAdaptor.User_to_GrpcUser(result.value())).build());
				responseObserver.onCompleted();
			} catch (Exception e){
				responseObserver.onError(errorCodeToStatus(ErrorCode.INTERNAL_ERROR));
			}
		}
	}

	@Override
	public void updateUser(UpdateUserArgs request, StreamObserver<UpdateUserResult> responseObserver) {
		User existingUser = impl.getUser(request.getName(),request.getPwd()).value();
		if (existingUser == null){
			responseObserver.onError(errorCodeToStatus(ErrorCode.NOT_FOUND));
		} else {
			Result<User> result = impl.updateUser(request.getName(),request.getPwd(),existingUser);
			if (!result.isOK()){
				responseObserver.onError(errorCodeToStatus(result.error()));
			} else {
				try {
					responseObserver.onNext(UpdateUserResult.newBuilder().setUser(DataModelAdaptor.User_to_GrpcUser(existingUser)).build());
					responseObserver.onCompleted();
				} catch (Exception e){
					responseObserver.onError(errorCodeToStatus(ErrorCode.INTERNAL_ERROR));
				}
			}
		}
	}

	@Override
	public void deleteUser(DeleteUserArgs request, StreamObserver<DeleteUserResult> responseObserver) {
	Result<User> result = impl.deleteUser(request.getName(),request.getPwd());
	if (!result.isOK()){
		responseObserver.onError(errorCodeToStatus(result.error()));
	} else {
		try {
			responseObserver.onNext(DeleteUserResult.newBuilder().setUser(DataModelAdaptor.User_to_GrpcUser(result.value())).build());
			responseObserver.onCompleted();
		} catch (Exception e){
			responseObserver.onError(errorCodeToStatus(ErrorCode.INTERNAL_ERROR));
		}
	}
	}

	@Override
	public void searchUsers(SearchUsersArgs request, StreamObserver<GrpcUser> responseObserver) {
		Result<List<User>> result = impl.searchUsers(request.getQuery());
		if (!result.isOK()){
			responseObserver.onError(errorCodeToStatus(result.error()));
		} else {
			try {
				for (User user : result.value()){
					responseObserver.onNext(DataModelAdaptor.User_to_GrpcUser(user));
				}
				responseObserver.onCompleted();
			} catch (Exception e){
				responseObserver.onError(errorCodeToStatus(ErrorCode.INTERNAL_ERROR));
			}
		}
	}

	@Override
	public void getUserPhoto(GetUserPhotoArgs request, StreamObserver<GetUserPhotoResult> responseObserver) {
		Result<byte[]> result = impl.getUserPhoto(request.getName(),request.getPwd());
		if (!result.isOK()){
			responseObserver.onError(errorCodeToStatus(result.error()));
		} else {
			try {
				responseObserver.onNext(GetUserPhotoResult.newBuilder().setPhoto(ByteString.copyFrom(result.value())).build());
				responseObserver.onCompleted();
			} catch (Exception e){
				responseObserver.onError(errorCodeToStatus(ErrorCode.INTERNAL_ERROR));
			}
		}
	}

	@Override
	public void updateUserPhoto(UpdateUserPhotoArgs request, StreamObserver<UpdateUserPhotoResult> responseObserver) {
		Result<User> result = impl.updateUserPhoto(request.getName(),request.getPwd(),request.getPhoto().toByteArray());
		if (!result.isOK()){
			responseObserver.onError(errorCodeToStatus(result.error()));
		} else {
			try {
				responseObserver.onNext(UpdateUserPhotoResult.newBuilder().setUser(DataModelAdaptor.User_to_GrpcUser(result.value())).build());
				responseObserver.onCompleted();
			} catch (Exception e){
				responseObserver.onError(errorCodeToStatus(ErrorCode.INTERNAL_ERROR));
			}
		}
	}
}
