package org.trickyplay.trickyplayapi.users.entities;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "RefreshTokens")
public class RefreshToken {
    /*
    comment on the use of Java Bean Validation:
    It’s not ideal to perform validation in the persistence layer, because it means that the business code above may have already worked with objects that could be invalid, which could lead to unexpected errors. However, just in case, it is worth securing the persistence layer using java bean validation as the last line of defense
    */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true, nullable = false)
    public String token;

    @NotNull
    @Column(nullable = false)
    public boolean revoked;

    @NotNull
    @ManyToOne(optional = false) // From the JPA 2.0 spec @ManyToOne has default FetchType.EAGER.
    @JoinColumn(name = "owner_user_id", referencedColumnName = "id", nullable = false)
    public TPUser owner;

    @NotNull
    @Column(nullable = false)
    private Instant expiryDate; //ISO-8601 UTC
}
