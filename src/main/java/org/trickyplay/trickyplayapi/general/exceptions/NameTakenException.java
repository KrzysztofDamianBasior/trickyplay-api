package org.trickyplay.trickyplayapi.general.exceptions;

public class NameTakenException extends RuntimeException {
    public NameTakenException(String message) {
        super(message);
    }
}