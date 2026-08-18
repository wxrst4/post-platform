package org.example.notificationsvc.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Table(name = "notifications")
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Operation operation;

    @Column(name = "post_id")
    private UUID postId;

    @Column(name = "post_title")
    private String postTitle;

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @Column(name = "created_at", nullable = false)
    @ColumnDefault("now()")
    private Instant createdAt;

    @Column(name = "is_read", nullable = false)
    @ColumnDefault("false")
    private Boolean isRead;

    @Column(name = "title")
    private String title;

    @PrePersist
    void prePersist() {
        if (isRead == null) {
            isRead = false;
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
