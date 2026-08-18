package org.example.contentsvc.application.channel;

import lombok.RequiredArgsConstructor;
import org.example.contentsvc.application.exceptions.ChannelAlreadyExistsException;
import org.example.contentsvc.application.exceptions.ChannelNotFoundException;
import org.example.contentsvc.domain.entity.Channel;
import org.example.contentsvc.infrastructure.repository.ChannelRepository;
import org.example.contentsvc.infrastructure.security.CurrentUserProvider;
import org.example.contentsvc.presentation.http.channel.dto.CreateChannelRequest;
import org.example.contentsvc.presentation.http.channel.dto.UpdateChannelRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public Channel create(CreateChannelRequest request) {

        if (channelRepository.existsByName(request.name())) {
            throw new ChannelAlreadyExistsException(request.name());
        }

        UUID ownerId = currentUserProvider.getUserId();

        Channel channel = Channel.builder()
                .ownerId(ownerId)
                .name(request.name())
                .description(request.description())
                .build();

        return channelRepository.save(channel);
    }

    public Channel getById(UUID id) {
        return channelRepository.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));
    }

    @Transactional
    public Channel update(UUID id, UpdateChannelRequest request) {
        Channel channel = getById(id);

        UUID ownerId = currentUserProvider.getUserId();

        checkOwner(channel, ownerId);

        if (!channel.getName().equals(request.name()) &&
                channelRepository.existsByName(request.name())) {
            throw new ChannelAlreadyExistsException(request.name());
        }

        channel.setName(request.name());
        channel.setDescription(request.description());

        return channel;
    }

    @Transactional
    public void delete(UUID id) {
        Channel channel = getById(id);
        UUID currentUserId = currentUserProvider.getUserId();
        checkOwner(channel, currentUserId);

        channelRepository.delete(channel);
    }

    private void checkOwner(Channel channel, UUID ownerId) {
        if (!channel.getOwnerId().equals(ownerId)) {
            throw new SecurityException("User is not the channel owner");
        }
    }

}
