package org.example.notificationsvc.presentation.http.mapper;

import org.example.notificationsvc.domain.entity.Notification;
import org.example.notificationsvc.presentation.http.dto.NotificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "type", source = "operation")
    NotificationDto toDto(Notification notification);

    List<NotificationDto> toDtos(List<Notification> notifications);
}
