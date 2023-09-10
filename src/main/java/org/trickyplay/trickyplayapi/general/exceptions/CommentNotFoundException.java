package org.trickyplay.trickyplayapi.general.exceptions;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(long id) {
        super("Could not find comment with id: " + id);
    }
}
