package org.example.notificationsvc.presentation.http;

import lombok.RequiredArgsConstructor;
import org.example.notificationsvc.application.notification.NotificationService;
import org.example.notificationsvc.domain.entity.Notification;
import org.example.notificationsvc.presentation.http.dto.NotificationDto;
import org.example.notificationsvc.presentation.http.mapper.NotificationMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    @GetMapping
    public List<NotificationDto> getNotifications(
            @RequestParam UUID recipientId,
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly
    ) {
        List<Notification> notifications = notificationService.getAll(recipientId, unreadOnly);
        return notificationMapper.toDtos(notifications);
    }

    @GetMapping("/unread-count")
    public long getUnreadCount(@RequestParam UUID recipientId) {
        return notificationService.getUnreadCount(recipientId);
    }

    @PatchMapping("/{id}/read")
    public NotificationDto markAsRead(@PathVariable UUID id) {
        Notification notification = notificationService.markAsRead(id);
        return notificationMapper.toDto(notification);
    }
}
