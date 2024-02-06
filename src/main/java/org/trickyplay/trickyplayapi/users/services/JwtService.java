package org.trickyplay.trickyplayapi.users.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    private String secretKey;
    private long jwtExpiration;

    public JwtService(@Value("${application.security.jwt.secret-key}") String secretKey,
                      @Value("${application.security.jwt.access-token-expiration}") long jwtExpiration){
        this.secretKey = secretKey;
        this.jwtExpiration = jwtExpiration;
    }

    private Key getSignInKey() {
        // ref: https://www.wikiwand.com/en/Base64
        // BASE64- Group of binary-to-text encoding schemes using 64 symbols that represent binary data (more specifically, a sequence of 8-bit bytes) in sequences of 24 bits that can be represented by four 6-bit Base64 digits.
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        // ref: https://www.wikiwand.com/en/JSON_Web_Token
        // Token signature is calculated by encoding the header and payload using Base64url Encoding RFC 4648 and concatenating the two together with a period separator. That string is then run through the cryptographic algorithm specified in the header.
        return Keys.hmacShaKeyFor(keyBytes); // Computes a Hash-based Message Authentication Code (HMAC) by using the SHA256 hash function.
    }

    private Claims extractAllClaims(String token) {
        // previous versions of the library used the Jwts.parser() method
        // in previous versions of the library Jwts.parser() that returns new JwtParser instance was recommended to use, now it is deprecated
        // Jwts.parser().setSigningKey(getSignInKey()).parseClaimsJws(token).getBody(); -deprecated, use parserBuilder() instead

        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    private List<String> getClaimOrEmptyList(String claim, String token) {
        // List<String> audience = extractAllClaims.get("aud", List.class);

        Claims claims = extractAllClaims(token);
        if (claims.get(claim) == null) return List.of();
        return claims.get(claim, List.class);
    }

    public Long extractTPUserId(String token) {
        return Long.parseLong(extractClaim(token, Claims::getSubject));
    }

    public String extractTPUserName(String token) {
        final Claims claims = extractAllClaims(token);
        return (String) claims.get("userName");
    }

    public Role extractTPUserRole(String token) {
        final Claims claims = extractAllClaims(token);
        return Role.valueOf((String) claims.get("userRole"));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public TPUserPrincipal extractPrincipal(String token) {
        final Claims claims = extractAllClaims(token);

        return TPUserPrincipal.builder()
                .id(Long.parseLong(claims.getSubject()))
                .name((String) claims.get("userName"))
                .password(null)
                .role((String) claims.get("userRole"))
                .createdAt(claims.get("userCreatedAt").toString())
                .updatedAt(claims.get("userUpdatedAt").toString())
                .build();
    }

    public String issueToken(TPUserPrincipal userDetails) {
        return issueToken(new HashMap<>(), userDetails);
    }

    public String issueToken(Map<String, Object> extraClaims, TPUserPrincipal userDetails) {
        return issueToken(extraClaims, userDetails, jwtExpiration);
    }

    private String issueToken(Map<String, Object> extraClaims, TPUserPrincipal userDetails, long expiration) {
        // Google JSON Style Guide (recommendations for building JSON APIs at Google) recommends that:
        //      1. Property names must be camelCased, ASCII strings.
        //      2. The first character must be a letter, an underscore (_), or a dollar sign ($)

        // issuer claim
        // ref: RFC 7519
        // The “iss” (issuer) claim identifies the principal that issued the JWT. The processing of this claim is generally application specific. The “iss” value is a case-sensitive string containing a StringOrURI value. Use of this claim is OPTIONAL.

        // subject claim
        // ref: RFC 7519
        // The “sub” (subject) claim identifies the principal that is the subject of the JWT. The claims in a JWT are normally statements about the subject. The subject value MUST either be scoped to be locally unique in the context of the issuer or be globally unique. The processing of this claim is generally application specific. The “sub” value is a case-sensitive string containing a StringOrURI value. Use of this claim is OPTIONAL
        // ref: OpenID Connect Core 1.0
        // REQUIRED. Subject Identifier. A locally unique and never reassigned identifier within the Issuer for the End-User, which is intended to be consumed by the Client, e.g., 24400320or AItOawmwtWwcT0k51BayewNvutrJUqsvl6qs7A4. It MUST NOT exceed 255 ASCII characters in length. The sub value is a case sensitive string.

        // expiration time claim
        // ref: RFC 7519
        // The “exp” (expiration time) claim identifies the expiration time on or after which the JWT MUST NOT be accepted for processing. The processing of the “exp” claim requires that the current date/time MUST be before the expiration date/time listed in the “exp” claim. Implementers MAY provide for some small leeway, usually no more than a few minutes, to account for clock skew. Its value MUST be a number containing a NumericDate value. Use of this claim is OPTIONAL.

        // issued at claim
        // RFC 7519
        // The “iat” (issued at) claim identifies the time at which the JWT was issued. This claim can be used to determine the age of the JWT. Its value MUST be a number containing a NumericDate value. Use of this claim is OPTIONAL.
        // OpenID Connect Core 1.0
        // REQUIRED. Time at which the JWT was issued. Its value is a JSON number representing the number of seconds from 1970–01–01T0:0:0Z as measured in UTC until the date/time.

        // alg parameter
        // The alg parameter in the header represents the algorithm of the signature. Valid values of alg are listed not in RFC 7515
        // JJwt is fully RFC specification compliant on all implemented functionality, tested against RFC-specified test vectors
        //SignatureAlgorithm.HS256 creates header- (alg: HS256), that means algorithm HMAC using SHA-256
        //  signature by RSA algorithm is binary data, even if we decode the signature field by base64url, what we can get is binary data
        List<String> authorities = new ArrayList<>();
        userDetails.getAuthorities().forEach(a -> authorities.add(a.getAuthority()));
        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("authorities", authorities);

        return Jwts.builder().setClaims(extraClaims)
                // .setIssuer()
                // .setAudience()
                // .setSubject(String.format("%s,%s", user.getId(), user.getName()))
                // .setId(UUID.randomUUID().toString()) //not needed for now
                .setSubject(userDetails.getId().toString())
                .setIssuedAt(new Date(System.currentTimeMillis())) // Instant.now()
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Instant.now().plusMillis(expiration) or Instant.now().toEpochMilli() + expiration
                .claim("userId", userDetails.getId().toString())
                .claim("userName", userDetails.getName())
                .claim("userRole", userDetails.getRole().name())
                .claim("userCreatedAt", userDetails.getCreatedAt().toString())
                .claim("userUpdatedAt", userDetails.getUpdatedAt().toString())
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .addClaims(additionalClaims)
                .compact();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateTokenBySignature(String token) {
        // When you use a JWT, you must check its signature before storing and using it.

        // You can enforce that the JWT you are parsing conforms to expectations that you require and are important for your application.
        // For example, let's say that you require that the JWT you are parsing has a specific sub (subject) value, otherwise you may not trust the token. You can do that by using one of the various require* methods

        try {
            Jwts.parser().setSigningKey(getSignInKey()).parseClaimsJws(token);

            // token is trustworthy and has not been tampered with
            return true;
        } catch (ExpiredJwtException ex) {
            log.error("token: " + token + " | " + "JWT expired", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("token: " + token + " | " + "Token is null, empty or only whitespace", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("token: " + token + " | " + "JWT is invalid", ex);
        } catch (UnsupportedJwtException ex) {
            log.error("token: " + token + " | " + "JWT is not supported", ex);
        } catch (SignatureException ex) {
            log.error("token: " + token + " | " + "Signature validation failed");
        }
        return false;
    }
}
