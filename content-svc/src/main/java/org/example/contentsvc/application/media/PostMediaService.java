package org.example.contentsvc.application.media;

import lombok.RequiredArgsConstructor;
import org.example.contentsvc.application.exceptions.PostNotFoundException;
import org.example.contentsvc.domain.entity.Post;
import org.example.contentsvc.domain.entity.PostMedia;
import org.example.contentsvc.infrastructure.repository.PostMediaRepository;
import org.example.contentsvc.infrastructure.repository.PostRepository;
import org.example.contentsvc.infrastructure.s3.S3StorageService;
import org.example.contentsvc.infrastructure.security.CurrentUserProvider;
import org.example.contentsvc.presentation.http.media.dto.PostMediaResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostMediaService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final CurrentUserProvider currentUserProvider;
    private final S3StorageService s3StorageService;

    public PostMediaResponse upload(UUID postId, MultipartFile file) {
        Post post = postRepository
                .findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        UUID userId = currentUserProvider.getUserId();
        if (!post.getAuthorId().equals(userId)) {
            throw new SecurityException("User is not post author");
        }

        validate(file);

        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();

        String objectKey = "posts/" + postId + "/images/" + UUID.randomUUID() + "-" + sanitizeFilename(originalName);

        byte[] content;

        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read uploaded file", exception);
        }

        S3StorageService.StoredObject stored = s3StorageService.upload(
                objectKey,
                originalName,
                file.getContentType(),
                content
        );

        PostMedia media = PostMedia.builder()
                .postId(postId)
                .objectKey(stored.key())
                .originalName(originalName)
                .contentType(stored.contentType())
                .sizeBytes(stored.sizeBytes())
                .build();

        PostMedia saved = postMediaRepository.save(media);

        return toResponse(saved);
    }

    public List<PostMediaResponse> getImages(UUID postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        return postMediaRepository
                .findAllByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PostMediaResponse toResponse(PostMedia media) {
        var presignedUrl = s3StorageService.createDownloadUrl(media.getObjectKey());

        return new PostMediaResponse(
                media.getId(),
                media.getPostId(),
                media.getOriginalName(),
                media.getContentType(),
                media.getSizeBytes(),
                presignedUrl.url(),
                presignedUrl.expiresAt()
        );
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Maximum image size is 10 MB");
        }

        String contentType = file.getContentType();

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Allowed image types: JPEG, PNG, WebP");
        }
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}