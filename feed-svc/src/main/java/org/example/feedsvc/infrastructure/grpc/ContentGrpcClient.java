package org.example.feedsvc.infrastructure.grpc;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.example.contentsvc.grpc.ContentServiceGrpc;
import org.example.contentsvc.grpc.PostResponse;
import org.example.contentsvc.grpc.PublishedPostsRequest;
import org.example.feedsvc.application.exception.ContentServiceUnavailableException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContentGrpcClient {

    private final ContentServiceGrpc.ContentServiceBlockingStub stub;

    public List<PostResponse> getPublishedPosts(List<UUID> channelIds, int limit, int offset
    ) {
        try {
            PublishedPostsRequest request = PublishedPostsRequest.newBuilder()
                    .addAllChannelIds(channelIds.stream()
                            .map(UUID::toString)
                            .toList()
                    )
                    .setLimit(limit)
                    .setOffset(offset)
                    .build();

            return stub.getPublishedPostsByChannels(request).getPostsList();
        } catch (StatusRuntimeException exception) {
            throw new ContentServiceUnavailableException(exception);
        }
    }
}
