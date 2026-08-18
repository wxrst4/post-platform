package org.example.feedsvc.infrastructure.grpc;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.example.feedsvc.application.exception.SocialServiceUnavailableException;
import org.example.socialsvc.grpc.SocialServiceGrpc;
import org.example.socialsvc.grpc.UserSubscriptionsRequest;
import org.example.socialsvc.grpc.UserSubscriptionsResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SocialGrpcClient {

    private final SocialServiceGrpc.SocialServiceBlockingStub stub;

    public List<UUID> getSubscriptions(UUID userId) {
        try {
            return stub.getUserSubscriptions(
                            UserSubscriptionsRequest.newBuilder()
                                    .setUserId(userId.toString())
                                    .build()
                    )
                    .getChannelIdsList()
                    .stream()
                    .map(UUID::fromString)
                    .toList();

        } catch (StatusRuntimeException exception) {
            throw new SocialServiceUnavailableException(exception);
        }
    }
}
