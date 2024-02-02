package org.trickyplay.trickyplayapi.replies.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import org.trickyplay.trickyplayapi.replies.entities.Reply;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findAllByParentCommentIdIn(List<Long> ids);

    Page<Reply> findAll(Pageable page);

    Page<Reply> findAllByParentCommentId(long parentCommentId, Pageable page);

    Page<Reply> findAllByAuthorName(String authorName, Pageable page);

    Page<Reply> findAllByAuthorId(long id, Pageable page);

    List<Reply> findAllByIdIn(List<Long> ids); // where x.id in ?1

    // The findAllRepliesWith query below is equivalent to the findAll query because the author field in the Reply entity is in the @ManyToOne relationship with the TPUser entity, and the default fetch type for @ManyToOne is fetch = FetchType.EAGER
    @Query("Select r from Reply r left join fetch r.author")
    Page<Reply> findAllRepliesWithAuthors(Pageable page);
}