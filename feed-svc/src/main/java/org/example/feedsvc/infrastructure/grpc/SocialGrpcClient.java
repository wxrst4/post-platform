package org.example.feedsvc.infrastructure.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.feedsvc.application.exception.SocialServiceUnavailableException;
import org.example.socialsvc.grpc.SocialServiceGrpc;
import org.example.socialsvc.grpc.UserSubscriptionsRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocialGrpcClient {

    private final SocialServiceGrpc.SocialServiceBlockingStub stub;

    public List<UUID> getSubscriptions(UUID userId) {
        log.info("Calling SocialService.GetUserSubscriptions, userId={}", userId);
        try {
            UserSubscriptionsRequest request = UserSubscriptionsRequest.newBuilder()
                    .setUserId(userId.toString())
                    .build();

            var response = stub
                    .withDeadlineAfter(3, java.util.concurrent.TimeUnit.SECONDS)
                    .getUserSubscriptions(request);

            log.info(
                    "SocialService.GetUserSubscriptions success, userId={}, channels={}",
                    userId,
                    response.getChannelIdsList()
            );

            return response.getChannelIdsList().stream()
                    .map(channelId -> {
                        try {
                            return UUID.fromString(channelId);
                        } catch (IllegalArgumentException e) {
                            log.error("Invalid channel UUID returned by social-svc: {}", channelId, e);
                            throw e;
                        }
                    }).toList();
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            log.error(
                    "gRPC SocialService.GetUserSubscriptions failed. " +
                            "userId={}, statusCode={}, description={}",
                    userId,
                    status.getCode(),
                    status.getDescription(),
                    e
            );
            throw new SocialServiceUnavailableException(e);
        } catch (Exception e) {
            log.error("Unexpected error while calling social-svc. userId={}", userId, e);
            throw e;
        }
    }
}