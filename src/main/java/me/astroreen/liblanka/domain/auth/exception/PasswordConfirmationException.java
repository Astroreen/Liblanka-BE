package me.astroreen.liblanka.domain.auth.exception;


import me.astroreen.liblanka.common.excpetion.ApiException;

public class PasswordConfirmationException extends RuntimeException implements ApiException {
    private static final String MESSAGE = "New password doesn't match confirmation password";
    public PasswordConfirmationException() {
        super(MESSAGE);
    }
}
