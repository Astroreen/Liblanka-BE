package me.astroreen.liblanka.domain.auth.exception;


import me.astroreen.liblanka.common.excpetion.ApiException;

public class IncorrectPasswordException extends RuntimeException implements ApiException {
    private static final String MESSAGE = "Incorrect password";
    public IncorrectPasswordException() {
        super(MESSAGE);
    }
}
