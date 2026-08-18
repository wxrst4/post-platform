package org.example.socialsvc.infrastructure.grpc;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.example.contentsvc.grpc.*;
import org.example.socialsvc.application.exceptions.ContentServiceUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContentGrpcClient {

    private final ContentServiceGrpc.ContentServiceBlockingStub stub;

    public boolean postExists(UUID postId) {
        PostResponse response = stub.getPost(PostRequest.newBuilder()
                .setPostId(postId.toString())
                .build()
        );

        return response.getExists();
    }

    public boolean channelExists(UUID channelId) {
        ChannelResponse response = stub.getChannel(ChannelRequest.newBuilder()
                .setChannelId(channelId.toString())
                .build()
        );

        return response.getExists();
    }

    public PostResponse getPost(UUID postId) {
        try {
            return stub.getPost(PostRequest.newBuilder()
                    .setPostId(postId.toString())
                    .build()
            );
        } catch (StatusRuntimeException exception) {
            throw new ContentServiceUnavailableException(exception);
        }
    }

    public ChannelResponse getChannel(UUID channelId) {
        try {
            return stub.getChannel(ChannelRequest.newBuilder()
                    .setChannelId(channelId.toString())
                    .build());
        } catch (StatusRuntimeException exception) {
            throw new ContentServiceUnavailableException(exception);
        }

    }
}
