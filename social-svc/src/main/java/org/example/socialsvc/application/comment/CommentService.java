package org.example.socialsvc.application.comment;

import lombok.RequiredArgsConstructor;
import org.example.socialsvc.application.exceptions.CommentNotFoundException;
import org.example.socialsvc.application.exceptions.PostNotFoundException;
import org.example.socialsvc.domain.entity.Comment;
import org.example.socialsvc.infrastructure.grpc.ContentGrpcClient;
import org.example.socialsvc.infrastructure.repository.CommentRepository;
import org.example.socialsvc.infrastructure.security.CurrentUserProvider;
import org.example.socialsvc.presentation.comment.dto.CreateCommentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ContentGrpcClient contentGrpcClient;

    @Transactional
    public Comment createComment(UUID postId, CreateCommentRequest request) {
        if (!contentGrpcClient.postExists(postId)) {
            throw new PostNotFoundException(postId);
        }

        UUID userId = currentUserProvider.getUserId();

        Comment comment = Comment.builder()
                .userId(userId)
                .postId(postId)
                .content(request.content())
                .build();

        return commentRepository.save(comment);
    }

    public List<Comment> findByPost(UUID postId) {
        return commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId);
    }

    @Transactional
    public void deleteComment(UUID commentId) {
        UUID userId = currentUserProvider.getUserId();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new SecurityException("User is not comment author");
        }

        commentRepository.delete(comment);
    }
}
