package org.example.contentsvc.application.exceptions;

public class ChannelAlreadyExistsException extends RuntimeException {
    public ChannelAlreadyExistsException(String name) {
        super("Channel already exists: " + name);
    }
}
