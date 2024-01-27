package org.trickyplay.trickyplayapi.replies.entities;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.Length;
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
    /*
    comment on the use of Java Bean Validation:
    It’s not ideal to perform validation in the persistence layer, because it means that the business code above may have already worked with objects that could be invalid, which could lead to unexpected errors. However, just in case, it is worth securing the persistence layer using java bean validation as the last line of defense
    */

    @Id // hibernate assumes that all @Id columns in the database will be NOT NULL and have unique index. So there is no need to add Column(nullable=false) declaration here
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Length(min = 1, max = 300)
    private String body;

    @NotNull
    @ManyToOne(optional=false) // default fetch type for @ManyToOne is fetch = FetchType.EAGER
    @JoinColumn(name = "author_user_id", referencedColumnName = "id", nullable = false)
    private TPUser author;

    @NotNull
    @ManyToOne(optional=false) // default fetch type for @ManyToOne is fetch = FetchType.EAGER
    @JoinColumn(name = "parent_comment_id", referencedColumnName = "id", nullable = false)
    private Comment parentComment;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime createdAt; //ISO-8601 UTC

    @NotNull
    @Column(nullable = false)
    private LocalDateTime updatedAt; //ISO-8601 UTC
}