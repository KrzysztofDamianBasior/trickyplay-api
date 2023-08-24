package org.trickyplay.trickyplayapi.comments.repositories;

import org.trickyplay.trickyplayapi.comments.entities.Comment;

import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;
import java.util.List;

public interface CommentRepository {
    @Query("Select c From Comment c")
    List<Comment> findAllComments(Pageable page);
}