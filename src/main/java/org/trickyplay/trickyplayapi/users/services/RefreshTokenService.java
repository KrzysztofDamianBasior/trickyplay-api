package org.trickyplay.trickyplayapi.users.services;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;
import org.trickyplay.trickyplayapi.users.entities.RefreshToken;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.repositories.RefreshTokenRepository;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Data
public class RefreshTokenService {
    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshExpiration;
    @Autowired
    private final RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private final TPUserRepository userRepository;

    public RefreshToken createAndSaveRefreshToken(String username) {
        RefreshToken refreshToken = RefreshToken.builder()
                .owner(userRepository.findByName(username).orElseThrow(() -> new UserNotFoundException(username)))
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken createAndSaveRefreshToken(Long id) {
        RefreshToken refreshToken = RefreshToken.builder()
                .owner(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)))
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken createAndSaveRefreshToken(TPUser owner) {
        RefreshToken refreshToken = RefreshToken.builder()
                .owner(owner)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public Boolean deleteTokenIfExpired(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
//            throw new RuntimeException(
//                    token.getToken() + " Refresh token was expired. Please make a new sign in request");
            return true;
        }
        return false;
    }

    public Boolean verifyIfTokenExpiredOrRevoked(RefreshToken token) {
        return token.getExpiryDate().compareTo(Instant.now()) < 0 || token.isRevoked();
    }

    public void revokeAllUserTokens(TPUser user) {
        var validUserTokens = refreshTokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty()) return;

        validUserTokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(validUserTokens);
    }
}

