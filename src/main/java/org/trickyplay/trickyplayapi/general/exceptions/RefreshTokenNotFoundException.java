package org.trickyplay.trickyplayapi.general.exceptions;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException(String token) {
        super("Could not find refresh token: " + token);
    }
}