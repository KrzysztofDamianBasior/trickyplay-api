package org.trickyplay.trickyplayapi.users.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.replies.entities.Reply;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "Users")
public class TPUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32, unique = true)
    private String name;

    @Column(nullable = false, length = 64)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "owner", orphanRemoval = true, cascade = CascadeType.ALL) // From the JPA 2.0 spec @OneToMany has default FetchType.LAZY. OrphanRemoval attribute in @OneToMany and @OneToOne is by default false. By default, no operations are cascaded.
    private List<RefreshToken> refreshTokens;

    @OneToMany(mappedBy = "author", orphanRemoval = true, cascade = CascadeType.ALL) // From the JPA 2.0 spec @OneToMany has default FetchType.LAZY. OrphanRemoval attribute in @OneToMany and @OneToOne is by default false. By default, no operations are cascaded.
    private List<Comment> comments;

    @OneToMany(mappedBy = "author", orphanRemoval = true, cascade = CascadeType.ALL) // From the JPA 2.0 spec @OneToMany has default FetchType.LAZY. OrphanRemoval attribute in @OneToMany and @OneToOne is by default false. By default, no operations are cascaded.
    private List<Reply> replies;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
