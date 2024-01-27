package org.trickyplay.trickyplayapi.comments.entities;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.Length;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.users.entities.TPUser;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "Comments")
public class Comment {
    /*
    comment on the use of Java Bean Validation:
    It’s not ideal to perform validation in the persistence layer, because it means that the business code above may have already worked with objects that could be invalid, which could lead to unexpected errors. However, just in case, it is worth securing the persistence layer using java bean validation as the last line of defense
    */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Length(min = 1, max = 300)
    private String body;

    @NotNull
    @ManyToOne(optional=false) // default fetch type for @ManyToOne is fetch = FetchType.EAGER
    @JoinColumn(name = "author_user_id", referencedColumnName = "id", nullable = false)
    private TPUser author;

    @OneToMany(mappedBy = "parentComment", orphanRemoval = true, cascade = CascadeType.ALL) // From the JPA 2.0 spec @OneToMany has default FetchType.LAZY. OrphanRemoval attribute in @OneToMany and @OneToOne is by default false. By default, no operations are cascaded.
    private List<Reply> replies;

    @NotNull
    @Pattern(regexp = "^(Snake|TicTacToe|Minesweeper)$", message = "invalid game name")
    private String gameName;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime createdAt; //ISO-8601 UTC

    @NotNull
    @Column(nullable = false)
    private LocalDateTime updatedAt; //ISO-8601 UTC
}
