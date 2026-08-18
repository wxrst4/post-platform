package org.example.socialsvc.application.exceptions;

import java.util.UUID;

public class SubscriptionAlreadyExistsException extends RuntimeException {
    public SubscriptionAlreadyExistsException(UUID userId, UUID channelId) {
        super("User " + userId + " is already subscribed to channel " + channelId);
    }
}
