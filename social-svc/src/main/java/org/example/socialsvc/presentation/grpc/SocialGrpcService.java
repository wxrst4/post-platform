package org.example.socialsvc.presentation.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.example.socialsvc.application.subscription.SubscriptionService;
import org.example.socialsvc.domain.entity.Subscription;
import org.example.socialsvc.grpc.SocialServiceGrpc;
import org.example.socialsvc.grpc.UserSubscriptionsRequest;
import org.example.socialsvc.grpc.UserSubscriptionsResponse;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class SocialGrpcService extends SocialServiceGrpc.SocialServiceImplBase {

    private final SubscriptionService subscriptionService;

    @Override
    public void getUserSubscriptions(
            UserSubscriptionsRequest request,
            StreamObserver<UserSubscriptionsResponse> responseObserver
    ) {
        try {
            UUID userId =
                    UUID.fromString(request.getUserId());

            List<UUID> channelIds = subscriptionService
                    .getSubscriptionsByUserId(userId)
                    .stream()
                    .map(Subscription::getChannelId)
                    .toList();

            UserSubscriptionsResponse response = UserSubscriptionsResponse.newBuilder()
                    .addAllChannelIds(channelIds
                            .stream()
                            .map(UUID::toString)
                            .toList()
                    ).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException exception) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription("Invalid userId").asRuntimeException()
            );
        }
    }
}
