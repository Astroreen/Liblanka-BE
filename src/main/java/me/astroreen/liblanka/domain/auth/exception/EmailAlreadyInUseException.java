package me.astroreen.liblanka.domain.auth.exception;


public class EmailAlreadyInUseException extends RuntimeException {
    private static final String MESSAGE = "Email already in use.";
    public EmailAlreadyInUseException() {super(MESSAGE);}
}
