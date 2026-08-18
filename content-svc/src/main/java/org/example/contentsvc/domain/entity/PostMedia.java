package org.example.contentsvc.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@Table(name = "post_media")
@NoArgsConstructor
@AllArgsConstructor
public class PostMedia {

    @Id
    @Column(name = "id", nullable = false)
    @ColumnDefault("gen_random_uuid()")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}