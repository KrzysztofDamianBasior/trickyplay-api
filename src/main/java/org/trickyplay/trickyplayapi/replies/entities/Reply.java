package org.trickyplay.trickyplayapi.replies.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.users.entities.TPUser;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "Replies")
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String body;

    @ManyToOne() // default fetch type for @ManyToOne is fetch = FetchType.EAGER
    @JoinColumn(name = "author_user_id", referencedColumnName = "id")
    private TPUser author;

    @ManyToOne() // default fetch type for @ManyToOne is fetch = FetchType.EAGER
    @JoinColumn(name = "parent_comment_id", referencedColumnName = "id")
    private Comment parentComment;

    private LocalDateTime createdAt; //ISO-8601 UTC

    private LocalDateTime updatedAt; //ISO-8601 UTC
}