package org.example.feedsvc.application.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.contentsvc.grpc.PostResponse;
import org.example.feedsvc.infrastructure.grpc.ContentGrpcClient;
import org.example.feedsvc.infrastructure.grpc.SocialGrpcClient;
import org.example.feedsvc.infrastructure.security.CurrentUserProvider;
import org.example.feedsvc.presentation.http.feed.dto.FeedItem;
import org.example.feedsvc.presentation.http.feed.dto.FeedResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final SocialGrpcClient socialGrpcClient;
    private final ContentGrpcClient contentGrpcClient;
    private final CurrentUserProvider currentUserProvider;

    public FeedResponse getFeed(int page, int size) {
        UUID userId = currentUserProvider.getUserId();
        int offset = page * size;
        List<UUID> channelIds = socialGrpcClient.getSubscriptions(userId);

        if (channelIds.isEmpty()) {
            return new FeedResponse(List.of(), page, size);
        }

        List<PostResponse> posts = contentGrpcClient.getPublishedPosts(channelIds, size, offset);

        posts.forEach(post -> log.info(
                "Feed post: id={}, channelId={}, authorId={}, status={}, publishedAt='{}'",
                post.getPostId(),
                post.getChannelId(),
                post.getAuthorId(),
                post.getStatus(),
                post.getPublishedAt()
        ));

        List<FeedItem> items = posts.stream()
                .map(this::toFeedItem)
                .toList();

        return new FeedResponse(items, page, size);
    }

    private FeedItem toFeedItem(PostResponse post) {
        return new FeedItem(
                UUID.fromString(post.getPostId()),
                UUID.fromString(post.getChannelId()),
                UUID.fromString(post.getAuthorId()),
                post.getTitle(),
                post.getContent(),
                parsePublishedAt(post.getPublishedAt())
        );
    }

    private Instant parsePublishedAt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        }
    }
}