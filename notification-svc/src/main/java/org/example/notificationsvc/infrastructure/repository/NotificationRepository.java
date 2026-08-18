package org.example.notificationsvc.infrastructure.repository;

import org.example.notificationsvc.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findAllByRecipientId(UUID recipientId);

    List<Notification> findAllByRecipientIdAndIsRead(UUID recipientId, Boolean isRead);

    long countByRecipientIdAndIsReadFalse(UUID recipientId);
}
