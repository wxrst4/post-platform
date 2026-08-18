package org.example.socialsvc.infrastructure.repository;

import org.example.socialsvc.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findAllByPostIdOrderByCreatedAtAsc(UUID postId);

    List<Comment> findAllByUserId(UUID userId);
}
