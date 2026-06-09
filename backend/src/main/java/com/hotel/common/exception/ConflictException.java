package com.hotel.common.exception;

public class ConflictException extends BusinessException {
    public ConflictException(String message) {
        super(409, message);
    }
}
