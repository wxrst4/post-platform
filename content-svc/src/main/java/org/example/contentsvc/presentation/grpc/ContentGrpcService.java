package org.example.contentsvc.presentation.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.example.contentsvc.application.channel.ChannelService;
import org.example.contentsvc.application.exceptions.ChannelNotFoundException;
import org.example.contentsvc.application.exceptions.PostNotFoundException;
import org.example.contentsvc.application.post.PostService;
import org.example.contentsvc.domain.entity.Channel;
import org.example.contentsvc.domain.entity.Post;
import org.example.contentsvc.grpc.*;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class ContentGrpcService extends ContentServiceGrpc.ContentServiceImplBase {

    private final PostService postService;
    private final ChannelService channelService;

    @Override
    public void getPost(PostRequest request, StreamObserver<PostResponse> responseObserver) {
        UUID postId;

        try {
            postId = UUID.fromString(request.getPostId());
        } catch (IllegalArgumentException e) {
            responseObserver.onNext(PostResponse.newBuilder()
                    .setExists(false)
                    .build()
            );
            responseObserver.onCompleted();
            return;
        }

        try {
            Post post = postService.getById(postId);

            PostResponse response = PostResponse.newBuilder()
                    .setExists(true)
                    .setPostId(post.getId().toString())
                    .setChannelId(post.getChannelId().toString())
                    .setAuthorId(post.getAuthorId().toString())
                    .setTitle(post.getTitle())
                    .setContent(post.getContent())
                    .setPublishedAt(post.getPublishedAt() == null ? "" : post.getPublishedAt().toString())
                    .setStatus(post.getStatus().name())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (PostNotFoundException exception) {
            responseObserver.onNext(
                    PostResponse.newBuilder()
                            .setExists(false)
                            .build()
            );
            responseObserver.onCompleted();
        }

    }

    @Override
    public void getChannel(ChannelRequest request, StreamObserver<ChannelResponse> responseObserver) {
        UUID channelId;

        try {
            channelId = UUID.fromString(request.getChannelId());
        } catch (IllegalArgumentException e) {
            responseObserver.onNext(ChannelResponse.newBuilder()
                    .setExists(false)
                    .build());
            responseObserver.onCompleted();
            return;
        }

        try {
            Channel channel = channelService.getById(channelId);

            ChannelResponse response = ChannelResponse.newBuilder()
                    .setExists(true)
                    .setChannelId(channel.getId().toString())
                    .setOwnerId(channel.getOwnerId().toString())
                    .setName(channel.getName())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ChannelNotFoundException exception) {
            responseObserver.onNext(ChannelResponse.newBuilder()
                    .setExists(false)
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getPublishedPostsByChannels(
            PublishedPostsRequest request,
            StreamObserver<PublishedPostsResponse> responseObserver
    ) {
        List<UUID> channelIds = request.getChannelIdsList()
                .stream()
                .map(UUID::fromString)
                .toList();

        int limit = request.getLimit();
        int offset = request.getOffset();

        if (limit <= 0) {
            limit = 20;
        }

        if (limit > 100) {
            limit = 100;
        }

        if (offset < 0) {
            offset = 0;
        }

        List<Post> posts = postService.findPublishedByChannels(
                channelIds,
                limit,
                offset
        );

        PublishedPostsResponse.Builder response = PublishedPostsResponse.newBuilder();

        for (Post post : posts) {
            response.addPosts(PostResponse.newBuilder()
                    .setExists(true)
                    .setPostId(post.getId().toString())
                    .setChannelId(post.getChannelId().toString())
                    .setAuthorId(post.getAuthorId().toString())
                    .setTitle(post.getTitle())
                    .setContent(post.getContent())
                    .setStatus(post.getStatus().name())
                    .setPublishedAt(post.getPublishedAt().toString())
                    .build()
            );
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
