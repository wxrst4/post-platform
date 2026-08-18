package org.example.feedsvc.presentation.http.feed.dto;

import java.time.Instant;
import java.util.UUID;

public record FeedItem(
        UUID postId,
        UUID channelId,
        UUID authorId,
        String title,
        String content,
        Instant publishedAt
) {
}