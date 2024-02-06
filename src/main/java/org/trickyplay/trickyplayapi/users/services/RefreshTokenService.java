package org.trickyplay.trickyplayapi.users.services;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    private final RefreshTokenRepository refreshTokenRepository;
    private final TPUserRepository userRepository;

    public RefreshToken createAndSaveRefreshToken(Long id) {
        RefreshToken refreshToken = RefreshToken.builder()
                .owner(userRepository.getReferenceById(id))
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(propagation = Propagation.REQUIRED)
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

    /**
     * Revoke all refresh tokens belonging to the user whose data is provided in the argument
     * @param user
     * @return returns the number of revoked tokens
     */
    public int revokeAllUserTokens(TPUser user) {
        var validUserTokens = refreshTokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty()) return 0;

        validUserTokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(validUserTokens);
        return validUserTokens.size();
    }
}
