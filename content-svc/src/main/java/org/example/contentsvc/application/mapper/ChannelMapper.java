package org.example.contentsvc.application.mapper;

import org.example.contentsvc.domain.entity.Channel;
import org.example.contentsvc.presentation.http.channel.dto.ChannelResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChannelMapper {

    ChannelResponse toResponse(Channel entity);
}
