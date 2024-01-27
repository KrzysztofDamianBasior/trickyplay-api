package org.trickyplay.trickyplayapi.users.entities;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.replies.entities.Reply;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "Users")
public class TPUser {
    /*
    comment on the use of Java Bean Validation:
    It’s not ideal to perform validation in the persistence layer, because it means that the business code above may have already worked with objects that could be invalid, which could lead to unexpected errors. However, just in case, it is worth securing the persistence layer using java bean validation as the last line of defense
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Length(min = 2, max = 16)
    @Pattern(regexp = "^[a-zA-Z0-9_]{2,16}$", message = "Username must contain between 2 and 16 characters. It can only consist of underscores, numbers, lowercase and uppercase letters.")
    @Column(nullable = false, length = 32, unique = true)
    private String name;

    @NotNull
    @Length(min = 4, max = 32)
    @Pattern(regexp = "^(?=.*[0-9])[a-zA-Z0-9_]{4,32}$", message = "Password must contain between 4 and 32 characters. It must contain at least one number. The password can only consist of underscores, numbers, lowercase and uppercase letters.")
    @Column(nullable = false, length = 64)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "owner", orphanRemoval = true, cascade = CascadeType.ALL)
    // From the JPA 2.0 spec @OneToMany has default FetchType.LAZY. OrphanRemoval attribute in @OneToMany and @OneToOne is by default false. By default, no operations are cascaded.
    private List<RefreshToken> refreshTokens;

    @OneToMany(mappedBy = "author", orphanRemoval = true, cascade = CascadeType.ALL)
    // From the JPA 2.0 spec @OneToMany has default FetchType.LAZY. OrphanRemoval attribute in @OneToMany and @OneToOne is by default false. By default, no operations are cascaded.
    private List<Comment> comments;

    @OneToMany(mappedBy = "author", orphanRemoval = true, cascade = CascadeType.ALL)
    // From the JPA 2.0 spec @OneToMany has default FetchType.LAZY. OrphanRemoval attribute in @OneToMany and @OneToOne is by default false. By default, no operations are cascaded.
    private List<Reply> replies;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
