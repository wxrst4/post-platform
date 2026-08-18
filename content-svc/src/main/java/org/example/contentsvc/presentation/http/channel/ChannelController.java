package org.example.contentsvc.presentation.http.channel;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.contentsvc.application.channel.ChannelService;
import org.example.contentsvc.application.mapper.ChannelMapper;
import org.example.contentsvc.domain.entity.Channel;
import org.example.contentsvc.presentation.http.channel.dto.ChannelResponse;
import org.example.contentsvc.presentation.http.channel.dto.CreateChannelRequest;
import org.example.contentsvc.presentation.http.channel.dto.UpdateChannelRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/channels")
public class ChannelController {

    private final ChannelService channelService;
    private final ChannelMapper channelMapper;

    @PostMapping
    public ResponseEntity<ChannelResponse> createChannel(
            @RequestBody @Valid CreateChannelRequest request
    ) {
        Channel channel = channelService.create(request);
        return ResponseEntity.ok(channelMapper.toResponse(channel));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChannelResponse> getChannel(@PathVariable UUID id) {
        Channel channel = channelService.getById(id);
        return ResponseEntity.ok(channelMapper.toResponse(channel));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChannelResponse> updateChannel(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateChannelRequest request
    ) {
        Channel channel = channelService.update(id, request);
        return ResponseEntity.ok(channelMapper.toResponse(channel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID id) {
        channelService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
