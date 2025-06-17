package me.astroreen.liblanka.domain.auth.exception;


public class IncorrectPasswordException extends RuntimeException{
    private static final String MESSAGE = "Incorrect password";
    public IncorrectPasswordException() {
        super(MESSAGE);
    }
}
