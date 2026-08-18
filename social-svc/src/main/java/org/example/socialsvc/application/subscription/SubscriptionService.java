package org.example.socialsvc.application.subscription;

import lombok.RequiredArgsConstructor;
import org.example.contentsvc.grpc.ChannelResponse;
import org.example.socialsvc.application.exceptions.ChannelNotFoundException;
import org.example.socialsvc.application.exceptions.SubscriptionAlreadyExistsException;
import org.example.socialsvc.application.subscription.event.NotificationRequestedEvent;
import org.example.socialsvc.application.subscription.event.PostPublishedEvent;
import org.example.socialsvc.application.subscription.kafka.NotificationEventProducer;
import org.example.socialsvc.domain.entity.Subscription;
import org.example.socialsvc.infrastructure.grpc.ContentGrpcClient;
import org.example.socialsvc.infrastructure.repository.SubscriptionRepository;
import org.example.socialsvc.infrastructure.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationEventProducer notificationEventProducer;
    private final ContentGrpcClient contentGrpcClient;

    @Transactional
    public Subscription subscribe(UUID channelId) {
        ChannelResponse channel = contentGrpcClient.getChannel(channelId);

        if (!channel.getExists()) {
            throw new ChannelNotFoundException(channelId);
        }

        UUID userId = currentUserProvider.getUserId();

        if (subscriptionRepository.existsByUserIdAndChannelId(userId, channelId)) {
            throw new SubscriptionAlreadyExistsException(userId, channelId);
        }

        Subscription subscription = Subscription.builder()
                .userId(userId)
                .channelId(channelId)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        if (!channel.getOwnerId().equals(userId)) {

            NotificationRequestedEvent event =
                    new NotificationRequestedEvent(
                            UUID.randomUUID(),
                            "SUBSCRIBE",
                            UUID.fromString(channel.getOwnerId()),
                            null,
                            channelId,
                            null,
                            "NEW SUBSCRIBER",
                            java.time.LocalDateTime.now()
                    );

            notificationEventProducer.sendAfterCommit(event);
        }

        return saved;
    }

    @Transactional
    public void unsubscribe(UUID channelId) {
        UUID userId = currentUserProvider.getUserId();

        subscriptionRepository.deleteByUserIdAndChannelId(userId, channelId);
    }

    public List<Subscription> getMySubscriptions() {
        UUID userId = currentUserProvider.getUserId();

        return subscriptionRepository.findAllByUserId(userId);
    }

    @Transactional
    public void handlePostPublished(PostPublishedEvent event) {
        List<Subscription> subscriptions = subscriptionRepository
                .findAllByChannelId(event.channelId());

        for (Subscription subscription : subscriptions) {
            UUID recipientId = subscription.getUserId();

            if (recipientId.equals(event.authorId())) {
                continue;
            }

            NotificationRequestedEvent notification = new NotificationRequestedEvent(
                    UUID.randomUUID(),
                    "NEW_POST",
                    recipientId,
                    event.postId(),
                    event.channelId(),
                    event.title(),
                    "NEW POST",
                    event.publishedAt()
            );

            notificationEventProducer.send(notification);
        }
    }

    public List<Subscription> getSubscriptionsByUserId(UUID userId) {
        return subscriptionRepository.findAllByUserId(userId);
    }
}
