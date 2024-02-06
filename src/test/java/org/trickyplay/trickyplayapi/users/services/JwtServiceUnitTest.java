package org.trickyplay.trickyplayapi.users.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;

import java.util.Date;

class JwtServiceUnitTest {
    private final String secretKey = "ESCTr+2uf8DKNIKHkRqdN6eM3V7nM1YM6b5Y1obBF4o=";
    private final String testJWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOiIzMDAwLTAyLTA2VDEwOjM2OjExLjUzN1oiLCJzdWIiOiIxIiwidXNlcklkIjoiMSIsInVzZXJOYW1lIjoidXNlciIsInVzZXJSb2xlIjoiVVNFUiIsInVzZXJDcmVhdGVkQXQiOiIyMDI0LTAyLTA2VDA0OjIyOjA3IiwidXNlclVwZGF0ZWRBdCI6IjIwMjQtMDItMDZUMDQ6MjI6MDciLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiLCJVU0VSX1JFQUQiLCJVU0VSX1VQREFURSIsIlVTRVJfREVMRVRFIiwiVVNFUl9DUkVBVEUiXX0.JCMLzp2E0Y-cCNOlozBvyashWoDcTbMHhZ7aHiAydls";

    @Test
    void given_JWTWithItsKey_when_extractClaimIsCalled_then_returnCorrespondingClaim() {
        JwtService jwtService = new JwtService(secretKey, 1);
        Date expiration = jwtService.extractClaim(testJWT, Claims::getExpiration);
        assertThat(expiration).isEqualTo("3000-02-06T11:36:11.537");
    }

    @Test
    void given_JWTWithItsKey_when_extractTPUserIdIsCalled_then_returnCorrespondingId() {
        JwtService jwtService = new JwtService(secretKey, 1);
        Long userId = jwtService.extractTPUserId(testJWT);
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    void given_JWTWithItsKey_when_extractTPUserNameIsCalled_then_returnCorrespondingName() {
        JwtService jwtService = new JwtService(secretKey, 1);
        String userName = jwtService.extractTPUserName(testJWT);
        assertThat(userName).isEqualTo("user");
    }

    @Test
    void given_JWTWithItsKey_when_extractTPUserRoleIsCalled_then_returnCorrespondingRole() {
        JwtService jwtService = new JwtService(secretKey, 1);
        Role userRole = jwtService.extractTPUserRole(testJWT);
        assertThat(userRole).isEqualTo(Role.USER);
    }

    @Test
    void given_JWTWithItsKey_when_extractExpirationIsCalled_then_returnCorrespondingExpiration() {
        JwtService jwtService = new JwtService(secretKey, 1);
        Date expiration = jwtService.extractExpiration(testJWT);
        assertThat(expiration).hasSameTimeAs("3000-02-06T11:36:11.537");
    }

    @Test
    void given_JWTWithItsKey_when_extractPrincipalIsCalled_then_returnCorrespondingPrincipal() {
        JwtService jwtService = new JwtService(secretKey, 1);
        TPUserPrincipal tPUserPrincipal = jwtService.extractPrincipal(testJWT);
        assertThat(tPUserPrincipal.getId()).isEqualTo(1L);
        assertThat(tPUserPrincipal.getName()).isEqualTo("user");
        assertThat(tPUserPrincipal.getAuthorities()).hasSameElementsAs(Role.USER.getAuthorities());
    }

    @Test
    void given_JWTWithItsKey_when_issueTokenIsCalled_then_returnCorrespondingToken() {
        TPUserPrincipal tPUserPrincipal = TPUserPrincipal.builder()
                .id(1L)
                .name("usere")
                .role(Role.USER.name())
                .createdAt("2024-02-06T04:22:07")
                .updatedAt("2024-02-06T04:22:07")
                .build();
        String token = new JwtService(secretKey, 1).issueToken(tPUserPrincipal);
        assertThat(token).matches("^[\\w-]*\\.[\\w-]*\\.[\\w-]*$");
    }

    @Test
    void given_JWTWithItsKey_when_isTokenExpiredIsCalled_then_returnCorrespondingBooleanValue() {
        JwtService jwtService = new JwtService(secretKey, 1);
        assertThat(jwtService.isTokenExpired(testJWT)).isFalse();
    }

    @Test
    void given_JWTWithItsKey_when_validateTokenBySignatureIsCalled_then_returnCorrespondingBooleanValue() {
        JwtService jwtService = new JwtService(secretKey, 1);
        assertThat(jwtService.validateTokenBySignature(testJWT)).isTrue();
    }
}