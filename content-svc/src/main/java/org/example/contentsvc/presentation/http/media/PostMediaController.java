package org.example.contentsvc.presentation.http.media;

import lombok.RequiredArgsConstructor;
import org.example.contentsvc.application.media.PostMediaService;
import org.example.contentsvc.presentation.http.media.dto.PostMediaResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/media")
public class PostMediaController {

    private final PostMediaService postMediaService;

    @PostMapping(
            value = "/{postId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<PostMediaResponse> uploadImage(
            @PathVariable UUID postId,
            @RequestParam("file") MultipartFile file
    ) {
        PostMediaResponse response = postMediaService.upload(postId, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/images")
    public ResponseEntity<List<PostMediaResponse>> getImages(@PathVariable UUID postId) {
        return ResponseEntity.ok(postMediaService.getImages(postId));
    }
}
