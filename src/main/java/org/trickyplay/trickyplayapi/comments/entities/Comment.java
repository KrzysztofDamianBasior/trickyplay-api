package org.trickyplay.trickyplayapi.comments.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String body;

    @ManyToOne() // default fetch type for @ManyToOne is fetch = FetchType.EAGER
    @JoinColumn(name = "author_user_id", referencedColumnName = "id")
    private TPUser author;

    @OneToMany(mappedBy = "parentComment", orphanRemoval = true, cascade = CascadeType.ALL) // From the JPA 2.0 spec @OneToMany has default FetchType.LAZY. OrphanRemoval attribute in @OneToMany and @OneToOne is by default false. By default, no operations are cascaded.
    private List<Reply> replies;

    private String gameName;

    private LocalDateTime createdAt; //ISO-8601 UTC

    private LocalDateTime updatedAt; //ISO-8601 UTC
}
