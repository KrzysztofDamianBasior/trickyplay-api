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

    // The findAllCommentsWithAuthors query below is equivalent to the findAll query because the author field in the Comment entity is in the @ManyToOne relationship with the TPUser entity, and the default fetch type for @ManyToOne is fetch = FetchType.EAGER
    @Query("Select c from Comment c left join fetch c.author")
    Page<Comment> findAllCommentsWithAuthors(Pageable page);
}
