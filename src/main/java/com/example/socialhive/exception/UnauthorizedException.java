package com.example.socialhive.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String msg){ super(msg); }
}
