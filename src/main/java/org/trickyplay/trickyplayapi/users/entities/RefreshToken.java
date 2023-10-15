package org.trickyplay.trickyplayapi.users.entities;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    public String token;

    public boolean revoked;

    @ManyToOne() // From the JPA 2.0 spec @ManyToOne has default FetchType.EAGER.
    @JoinColumn(name = "owner_user_id", referencedColumnName = "id")
    public TPUser owner;

    private Instant expiryDate; //ISO-8601 UTC
}
