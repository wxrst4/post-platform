package org.example.socialsvc.presentation.like;

import lombok.RequiredArgsConstructor;
import org.example.socialsvc.application.like.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/likes")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> like(@PathVariable UUID postId) {
        likeService.like(postId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/unlike")
    public ResponseEntity<Void> unlike(@PathVariable UUID postId) {
        likeService.unlike(postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/count")
    public long count(@PathVariable UUID postId) {
        return likeService.countByPostId(postId);
    }
}
