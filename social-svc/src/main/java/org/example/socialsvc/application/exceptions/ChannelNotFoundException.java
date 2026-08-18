package org.example.socialsvc.application.exceptions;

import java.util.UUID;

public class ChannelNotFoundException extends RuntimeException {
    public ChannelNotFoundException(UUID channelId) {
        super("Channel with id " + channelId + " not found");
    }
}
