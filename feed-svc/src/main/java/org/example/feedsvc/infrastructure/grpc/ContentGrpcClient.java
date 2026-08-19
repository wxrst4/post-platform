package org.example.feedsvc.infrastructure.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.contentsvc.grpc.ContentServiceGrpc;
import org.example.contentsvc.grpc.PostResponse;
import org.example.contentsvc.grpc.PublishedPostsRequest;
import org.example.feedsvc.application.exception.ContentServiceUnavailableException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentGrpcClient {

    private final ContentServiceGrpc.ContentServiceBlockingStub stub;

    public List<PostResponse> getPublishedPosts(List<UUID> channelIds, int limit, int offset) {
        log.info(
                "Calling ContentService.GetPublishedPostsByChannels, channels={}, limit={}, offset={}",
                channelIds,
                limit,
                offset
        );
        try {
            PublishedPostsRequest request = PublishedPostsRequest.newBuilder()
                    .addAllChannelIds(channelIds.stream()
                            .map(UUID::toString)
                            .toList()
                    )
                    .setLimit(limit)
                    .setOffset(offset)
                    .build();

            var response = stub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .getPublishedPostsByChannels(request);

            log.info(
                    "ContentService.GetPublishedPostsByChannels success, posts={}",
                    response.getPostsCount()
            );
            return response.getPostsList();
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            log.error(
                    "ContentService.GetPublishedPostsByChannels failed. " +
                            "statusCode={}, description={}, channels={}, limit={}, offset={}",
                    status.getCode(),
                    status.getDescription(),
                    channelIds,
                    limit,
                    offset,
                    e
            );

            throw new ContentServiceUnavailableException(e);
        } catch (Exception e) {
            log.error(
                    "Unexpected error while calling content-svc. channels={}, limit={}, offset={}",
                    channelIds,
                    limit,
                    offset,
                    e
            );
            throw e;
        }
    }
}