package org.trickyplay.trickyplayapi.users.entities;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.trickyplay.trickyplayapi.auth.entities.RefreshToken;
import org.trickyplay.trickyplayapi.auth.enums.Role;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.replies.entities.Reply;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32, unique = true)
    private String name;

    @Column(nullable = false, length = 64)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role roles;

    @OneToMany(mappedBy = "user", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RefreshToken> refreshTokens;

    @OneToMany(mappedBy = "author", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(mappedBy = "author", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Reply> replies;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
