package org.example.feedsvc.presentation.http.feed.dto;

import java.util.List;

public record FeedResponse(
        List<FeedItem> content,
        int page,
        int size
) {
}