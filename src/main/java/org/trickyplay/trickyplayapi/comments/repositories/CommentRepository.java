package org.trickyplay.trickyplayapi.comments.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import org.trickyplay.trickyplayapi.comments.entities.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAll(Pageable page);

    Page<Comment> findAllByAuthorId(Long authorId, Pageable page);

    Page<Comment> findAllByAuthorName(String authorName, Pageable page);

    Page<Comment> findAllByGameName(String gameName, Pageable page); // where x.gameName == gameName

    List<Comment> findAllByIdIn(List<Long> ids); // where x.id in ?1

    @Query("Select c from Comment c left join fetch c.author")
    List<Comment> findAllCommentsWithAuthors(Pageable page);
}
