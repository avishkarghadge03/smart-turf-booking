package com.smartturf.exception;

/**
 * Exception thrown for business logic violations during booking, cancellation, or payments.
 */
public class BookingException extends RuntimeException {

    public BookingException(String message) {
        super(message);
    }
}
