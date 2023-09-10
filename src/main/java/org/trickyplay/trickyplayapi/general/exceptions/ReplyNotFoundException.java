package org.trickyplay.trickyplayapi.general.exceptions;

public class ReplyNotFoundException extends RuntimeException {
    public ReplyNotFoundException(long id) {
        super("Could not find reply with id: " + id);
    }
}