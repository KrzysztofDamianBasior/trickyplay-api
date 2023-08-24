package org.trickyplay.trickyplayapi.replies.repositories;

import org.trickyplay.trickyplayapi.replies.entities.Reply;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findAllByParentCommentIdIn(List<Long> ids);
}