package org.example.socialsvc.application.like;

import lombok.RequiredArgsConstructor;
import org.example.contentsvc.grpc.PostResponse;
import org.example.socialsvc.application.exceptions.LikeAlreadyExistsException;
import org.example.socialsvc.application.exceptions.PostNotFoundException;
import org.example.socialsvc.application.subscription.event.NotificationRequestedEvent;
import org.example.socialsvc.application.subscription.kafka.NotificationEventProducer;
import org.example.socialsvc.domain.entity.Like;
import org.example.socialsvc.infrastructure.grpc.ContentGrpcClient;
import org.example.socialsvc.infrastructure.repository.LikeRepository;
import org.example.socialsvc.infrastructure.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ContentGrpcClient contentGrpcClient;
    private final NotificationEventProducer notificationEventProducer;

    @Transactional
    public void like(UUID postId) {
        PostResponse post = contentGrpcClient.getPost(postId);

        if (!post.getExists()) {
            throw new PostNotFoundException(postId);
        }

        if (!post.getStatus().equals("PUBLISHED")) {
            throw new IllegalStateException("Only published posts can be liked");
        }

        UUID userId = currentUserProvider.getUserId();

        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new LikeAlreadyExistsException(userId, postId);
        }

        Like like = Like.builder()
                .postId(postId)
                .userId(userId)
                .build();
        likeRepository.save(like);

        if (!post.getAuthorId().equals(userId)) {
            NotificationRequestedEvent event = new NotificationRequestedEvent(
                    UUID.randomUUID(),
                    "LIKE",
                    UUID.fromString(post.getAuthorId()),
                    postId,
                    UUID.fromString(post.getChannelId()),
                    post.getTitle(),
                    "LIKE",
                    java.time.LocalDateTime.now()
            );

            notificationEventProducer.sendAfterCommit(event);
        }
    }

    @Transactional
    public void unlike(UUID postId) {
        UUID userId = currentUserProvider.getUserId();
        likeRepository.deleteByUserIdAndPostId(userId, postId);
    }

    public long countByPostId(UUID postId) {
        return likeRepository.countByPostId(postId);
    }

}
