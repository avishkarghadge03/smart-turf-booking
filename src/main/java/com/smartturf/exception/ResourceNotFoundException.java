package com.smartturf.exception;

/**
 * Exception thrown when a requested database entity is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
