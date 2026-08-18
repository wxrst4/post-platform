package org.example.contentsvc.application.post.kafka;

import lombok.RequiredArgsConstructor;
import org.example.contentsvc.application.post.event.PostPublishedEvent;
import org.example.contentsvc.domain.entity.Post;
import org.example.contentsvc.infrastructure.kafka.KafkaService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostEventPublisher {

    private static final String POST_PUBLISHED = "post-published";

    private final KafkaService kafkaService;
    private final KafkaTemplate<String, PostPublishedEvent> postPublishedKafkaTemplate;

    public void publishPostPublished(Post post) {
        var event = new PostPublishedEvent(
                UUID.randomUUID(),
                post.getId(),
                post.getChannelId(),
                post.getAuthorId(),
                post.getTitle(),
                post.getPublishedAt()
        );

        kafkaService.sendAfterCommit(
                POST_PUBLISHED,
                postPublishedKafkaTemplate,
                post.getId().toString(),
                event
        );
    }
}
