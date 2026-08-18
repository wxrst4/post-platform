package org.example.contentsvc.application.exceptions;

import java.util.UUID;

public class ChannelNotFoundException extends RuntimeException {
    public ChannelNotFoundException(UUID id) {
        super("Channel with id: " + id + " not found");
    }
}
