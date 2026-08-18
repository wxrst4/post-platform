package org.example.contentsvc.application.post;

import lombok.RequiredArgsConstructor;
import org.example.contentsvc.application.channel.ChannelService;
import org.example.contentsvc.application.exceptions.PostNotFoundException;
import org.example.contentsvc.application.post.kafka.PostEventPublisher;
import org.example.contentsvc.domain.entity.Channel;
import org.example.contentsvc.domain.entity.Post;
import org.example.contentsvc.domain.entity.PostStatus;
import org.example.contentsvc.infrastructure.repository.PostRepository;
import org.example.contentsvc.infrastructure.security.CurrentUserProvider;
import org.example.contentsvc.presentation.http.post.dto.CreatePostRequest;
import org.example.contentsvc.presentation.http.post.dto.UpdatePostRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ChannelService channelService;
    private final PostEventPublisher postEventPublisher;

    @Transactional
    public Post create(CreatePostRequest request) {
        UUID userId = currentUserProvider.getUserId();

        Channel channel = channelService.getById(request.channelId());

        checkChannelOwner(channel.getOwnerId(), userId);

        Post post = Post.builder()
                .channelId(channel.getId())
                .authorId(userId)
                .title(request.title())
                .content(request.content())
                .status(PostStatus.DRAFT)
                .build();

        return postRepository.save(post);
    }

    public Post getById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    public List<Post> getPublishedByChannel(UUID channelId) {
        return postRepository.findAllByChannelIdAndStatus(channelId, PostStatus.PUBLISHED);
    }

    @Transactional
    public Post update(UUID postId, UpdatePostRequest request) {
        Post post = getById(postId);
        UUID userId = currentUserProvider.getUserId();
        checkAuthor(post, userId);

        if (post.getStatus() == PostStatus.DELETED) {
            throw new IllegalStateException("Deleted post cannot be updated");
        }

        post.setTitle(request.title());
        post.setContent(request.content());

        if (post.getStatus() == PostStatus.PUBLISHED) {
            post.setStatus(PostStatus.PENDING_MODERATION);
            post.setPublishedAt(null);
        }

        return post;
    }

    @Transactional
    public void delete(UUID id) {
        Post post = getById(id);
        UUID userId = currentUserProvider.getUserId();

        checkAuthor(post, userId);

        post.setStatus(PostStatus.DELETED);
    }

    @Transactional
    public Post sendToModeration(UUID postId) {
        Post post = getById(postId);
        UUID userId = currentUserProvider.getUserId();
        checkAuthor(post, userId);

        if (post.getStatus() != PostStatus.DRAFT) {
            throw new IllegalStateException("Only draft post can be sent to moderation");
        }
        post.setStatus(PostStatus.PENDING_MODERATION);

        return post;
    }

    @Transactional
    public Post publish(UUID postId) {
        Post post = getById(postId);
        UUID userId = currentUserProvider.getUserId();
        checkAuthor(post, userId);

        if (post.getStatus() != PostStatus.PENDING_MODERATION) {
            throw new IllegalStateException("Post must be pending moderation");
        }
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        postEventPublisher.publishPostPublished(post);

        return post;
    }

    @Transactional
    public Post hide(UUID id) {
        Post post = getById(id);
        UUID userId = currentUserProvider.getUserId();
        checkAuthor(post, userId);

        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new IllegalStateException("Only published post can be hidden");
        }
        post.setStatus(PostStatus.HIDDEN);

        return post;
    }

    public List<Post> findPublishedByChannels(List<UUID> channelIds, int limit, int offset) {
        if (channelIds == null || channelIds.isEmpty()) {
            return List.of();
        }

        Pageable pageable = PageRequest
                .of(offset / limit, limit,
                        Sort.by(
                                Sort.Direction.DESC,
                                "publishedAt"
                        )
                );

        return postRepository
                .findAllByChannelIdInAndStatus(channelIds, PostStatus.PUBLISHED, pageable)
                .getContent();
    }

    private void checkChannelOwner(UUID ownerId, UUID userId) {
        if (!ownerId.equals(userId)) {
            throw new SecurityException("User is not the channel owner");
        }
    }

    private void checkAuthor(Post post, UUID userId) {
        if (!post.getAuthorId().equals(userId)) {
            throw new SecurityException("User is not the post author");
        }
    }
}
