package org.trickyplay.trickyplayapi.users.services;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;
import org.trickyplay.trickyplayapi.users.entities.RefreshToken;
import org.trickyplay.trickyplayapi.users.repositories.RefreshTokenRepository;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
@Service
@Slf4j
@Data
public class RefreshTokenService {
    @Autowired
    private final RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private final TPUserRepository userRepository;

    public RefreshToken createRefreshToken(String username) {
        RefreshToken refreshToken = RefreshToken.builder()
                .owner(userRepository.findByName(username).orElseThrow(() -> new UserNotFoundException(username)))
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(600000))// 10
                .revoked(false)
                .expired(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException(
                    token.getToken() + " Refresh token was expired. Please make a new sign in request");
        }
        return token;
    }
}
