package org.example.contentsvc.infrastructure.repository;

import org.example.contentsvc.domain.entity.Post;
import org.example.contentsvc.domain.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findAllByChannelIdAndStatus(UUID channelId, PostStatus status);

    Page<Post> findAllByChannelIdInAndStatus(
            List<UUID> channelIds,
            PostStatus status,
            Pageable pageable
    );
}
