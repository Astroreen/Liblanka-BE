package me.astroreen.liblanka.domain.auth.exception;


import me.astroreen.liblanka.common.excpetion.ApiException;

public class EmailAlreadyInUseException extends RuntimeException implements ApiException {
    private static final String MESSAGE = "Email already in use.";
    public EmailAlreadyInUseException() {super(MESSAGE);}
}
