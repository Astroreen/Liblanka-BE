package me.astroreen.liblanka.domain.auth.exception;


public class PasswordConfirmationException extends RuntimeException{
    private static final String MESSAGE = "New password doesn't match confirmation password";
    public PasswordConfirmationException() {
        super(MESSAGE);
    }
}
