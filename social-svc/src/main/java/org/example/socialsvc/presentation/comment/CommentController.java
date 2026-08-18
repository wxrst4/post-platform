package org.example.socialsvc.presentation.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.socialsvc.application.comment.CommentService;
import org.example.socialsvc.application.mapper.CommentMapper;
import org.example.socialsvc.domain.entity.Comment;
import org.example.socialsvc.presentation.comment.dto.CommentResponse;
import org.example.socialsvc.presentation.comment.dto.CreateCommentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @PostMapping("/posts/{postId}")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CreateCommentRequest request) {
        Comment comment = commentService.createComment(postId, request);

        return ResponseEntity.ok(commentMapper.toResponse(comment));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<List<CommentResponse>> getAllComments(@PathVariable UUID postId) {
        List<Comment> comments = commentService.findByPost(postId);

        return ResponseEntity.ok(commentMapper.toResponse(comments));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
