package org.example.socialsvc.application.subscription.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.socialsvc.application.subscription.SubscriptionService;
import org.example.socialsvc.application.subscription.event.PostPublishedEvent;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostPublishedConsumerHandler {

    private final SubscriptionService subscriptionService;

    public void consume(ConsumerRecord<String, PostPublishedEvent> event) {
        PostPublishedEvent postEvent = event.value();

        if (postEvent == null) {
            return;
        }

        log.info(
                "Received POST_PUBLISHED. postId={}, channelId={}",
                postEvent.postId(),
                postEvent.channelId()
        );

        subscriptionService.handlePostPublished(postEvent);
    }
}
