package org.trickyplay.trickyplayapi.general.exceptions;

public class RefreshTokenExpiredOrRevokedException extends RuntimeException {
    public RefreshTokenExpiredOrRevokedException(String token) {
        super("Token: " + token + " expired or revoked");
    }
}
