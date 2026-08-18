package org.example.contentsvc.presentation.http.post;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.contentsvc.application.post.PostService;
import org.example.contentsvc.application.mapper.PostMapper;
import org.example.contentsvc.domain.entity.Post;
import org.example.contentsvc.presentation.http.post.dto.CreatePostRequest;
import org.example.contentsvc.presentation.http.post.dto.PostResponse;
import org.example.contentsvc.presentation.http.post.dto.UpdatePostRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;

    @PostMapping
    public ResponseEntity<PostResponse> create(
            @RequestBody @Valid CreatePostRequest request
    ) {
        Post post = postService.create(request);
        return ResponseEntity.ok(postMapper.toResponse(post));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable UUID id) {
        Post post = postService.getById(id);
        return ResponseEntity.ok(postMapper.toResponse(post));
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<List<PostResponse>> getPublishedByChannel(
            @PathVariable UUID channelId
    ) {
        List<Post> posts = postService.getPublishedByChannel(channelId);
        return ResponseEntity.ok(postMapper.toResponse(posts));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdatePostRequest request
    ) {
        Post post = postService.update(id, request);
        return ResponseEntity.ok(postMapper.toResponse(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/moderation")
    public ResponseEntity<PostResponse> sendToModeration(@PathVariable UUID id) {
        Post post = postService.sendToModeration(id);
        return ResponseEntity.ok(postMapper.toResponse(post));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<PostResponse> publish(@PathVariable UUID id) {
        Post post = postService.publish(id);
        return ResponseEntity.ok(postMapper.toResponse(post));
    }

    @PostMapping("/{id}/hide")
    public ResponseEntity<PostResponse> hide(@PathVariable UUID id) {
        Post post = postService.hide(id);
        return ResponseEntity.ok(postMapper.toResponse(post));
    }
}
