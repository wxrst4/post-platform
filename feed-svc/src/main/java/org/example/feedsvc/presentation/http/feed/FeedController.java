package org.example.feedsvc.presentation.http.feed;

import lombok.RequiredArgsConstructor;
import org.example.feedsvc.application.feed.FeedService;
import org.example.feedsvc.presentation.http.feed.dto.FeedResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feed")
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public FeedResponse getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return feedService.getFeed(page, size);
    }
}
