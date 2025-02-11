package org.trickyplay.trickyplayapi.users.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.trickyplay.trickyplayapi.general.exceptions.RefreshTokenExpiredOrRevokedException;
import org.trickyplay.trickyplayapi.general.exceptions.RefreshTokenNotFoundException;
import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;
import org.trickyplay.trickyplayapi.users.controllers.AuthenticationController;
import org.trickyplay.trickyplayapi.users.dtos.*;
import org.trickyplay.trickyplayapi.users.entities.RefreshToken;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.RefreshTokenRepository;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    private final TPUserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public SignInResponse signIn(SignInRequest signInRequest) {
        // ref: https://docs.spring.io/spring-security/site/docs/3.0.x/reference/technical-overview.html
        // ref: https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html
        // 1. The username and password are obtained and combined into an instance of UsernamePasswordAuthenticationToken
        // 2. The token is passed to an instance of AuthenticationManager for validation.
        // 3. The AuthenticationManager returns a fully populated Authentication instance on successful authentication.
        // 4. The security context is established by calling SecurityContextHolder.getContext().setAuthentication(...), passing in the returned authentication object

        // The Authentication interface serves two main purposes within Spring Security:
        // 1. An input to AuthenticationManager to provide the credentials a user has provided to authenticate. When used in this scenario, isAuthenticated() returns false.
        // 2. Represent the currently authenticated user

        // The Authentication contains:
        // 1. principal: Identifies the user. When authenticating with a username/password this is often an instance of UserDetails.
        // 2. credentials: Often a password. In many cases, this is cleared after the user is authenticated, to ensure that it is not leaked.
        // 3. authorities: The GrantedAuthority instances are high-level permissions the user is granted. Two examples are roles and scopes. You can obtain GrantedAuthority instances from the Authentication.getAuthorities() method. This method provides a Collection of GrantedAuthority objects.

        // let's use an authentication manager to authenticate the user
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()));
        // Spring Security does not care what type of Authentication implementation is set on the SecurityContext. Here, we use UsernamePasswordAuthenticationToken  - UsernamePasswordAuthenticationToken(userDetails, password, authorities).
        SecurityContextHolder.getContext().setAuthentication(authentication); // according to docs- https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html -now we set the SecurityContext on the SecurityContextHolder. Spring Security uses this information for authorization.


        // ref: https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html
        // By default, SecurityContextHolder uses a ThreadLocal to store these details, which means that the SecurityContext is always available to methods in the same thread, even if the SecurityContext is not explicitly passed around as an argument to those methods. Using a ThreadLocal in this way is quite safe if you take care to clear the thread after the present principal’s request is processed. Spring Security’s FilterChainProxy ensures that the SecurityContext is always cleared.
        // To obtain information about the authenticated principal, access the SecurityContextHolder.
        // SecurityContext context = SecurityContextHolder.getContext();
        // Authentication authentication = context.getAuthentication();
        // String username = authentication.getName();
        // Object principal = authentication.getPrincipal();
        // Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Possible exceptions
        // ref: https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/AuthenticationException.html
        // AuthenticationException- Abstract superclass for all exceptions related to an Authentication object being invalid for whatever reason.
        // BadCredentialsException- Thrown if an authentication request is rejected because the credentials are invalid. For this exception to be thrown, it means the account is neither locked nor disabled.
        // try {
        //     Authentication authentication = authenticationManager.authenticate
        // } catch (AuthenticationException e) {
        //   SecurityContextHolder.getContext().setAuthentication(null);
        //   LOGGER.warn("auth error:{}", e.getMessage());
        // }
        //        if (authentication.isAuthenticated()) {
        //        } else {
        //            throw new UsernameNotFoundException("invalid user request!");
        //        }

        // TPUserPrincipal principal = userDetailsService.loadUserByUsername(username);
        var principal = (TPUserPrincipal) authentication.getPrincipal();

        var accessToken = jwtService.issueToken(principal);
        var refreshToken = refreshTokenService.createAndSaveRefreshToken(principal.getId());
        log.debug("Successfully authenticated. Security context contains: " + SecurityContextHolder.getContext().getAuthentication());
        SignInResponse signInResponse = SignInResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.token)
                .userPublicInfo(TPUserRepresentation.builder()
                        .id(principal.getId())
                        .name(principal.getName())
                        .role(principal.getRole())
                        .updatedAt(principal.getUpdatedAt())
                        .createdAt(principal.getCreatedAt())
                        .build())
                .build();
        signInResponse.add(linkTo(methodOn(AuthenticationController.class)
                .singleSessionSignOut(new RefreshTokenRequest("verySecretRefreshToken")))
                .withRel("singleSessionLogout"));
        signInResponse.add(linkTo(methodOn(AuthenticationController.class)
                .allSessionsSignOut())
                .withRel("allSessionsLogout"));
        signInResponse.add(linkTo(methodOn(AuthenticationController.class)
                .refreshAccessToken(new RefreshTokenRequest("verySecretRefreshToken")))
                .withRel("refreshAccessToken"));
        signInResponse.add(linkTo(methodOn(AuthenticationController.class)
                .signIn(new SignInRequest("username", "verySecretPassword")))
                .withSelfRel());
        return signInResponse;
    }

    public SignOutResponse singleSessionLogout(String refreshToken) {
        RefreshToken storedToken = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RefreshTokenNotFoundException(refreshToken));

        storedToken.setRevoked(true);
        tokenRepository.save(storedToken);
        SecurityContextHolder.clearContext();

        SignOutResponse signOutResponse = SignOutResponse.builder()
                .message("successfully signed out")
                .numberOfRefreshTokensRemoved(1)
                .build();
        signOutResponse.add(linkTo(methodOn(AuthenticationController.class)
                .signIn(new SignInRequest("username", "verySecretPassword")))
                .withRel("signIn"));
        signOutResponse.add(linkTo(methodOn(AuthenticationController.class)
                .singleSessionSignOut(new RefreshTokenRequest("verySecretRefreshToken")))
                .withSelfRel());
        return signOutResponse;
    }

    public SignOutResponse allSessionsLogout(Long userId) {
        TPUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        int numberOfRevokedUsers = refreshTokenService.revokeAllUserTokens(user);
        String message = "successfully signed out of all sessions";
        if (numberOfRevokedUsers == 0) {
            message = "not a single account was signed out";
        }
        SignOutResponse signOutResponse = SignOutResponse.builder()
                .message(message)
                .numberOfRefreshTokensRemoved(numberOfRevokedUsers)
                .build();
        signOutResponse.add(linkTo(methodOn(AuthenticationController.class)
                .allSessionsSignOut())
                .withSelfRel());
        signOutResponse.add(linkTo(methodOn(AuthenticationController.class)
                .signIn(new SignInRequest(user.getName(), "verySecretPassword")))
                .withRel("signIn"));
        return signOutResponse;
    }

    @Transactional
    public SignInResponse signUp(SignUpRequest request) {
        var user = TPUser.builder()
                .name(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .refreshTokens(null)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        // if we used a separate table in the database to store user roles:
        // UserEntity user = new UserEntity();
        // user.setUsername(registerDto.getUsername());
        // user.setPassword(passwordEncoder.encode((registerDto.getPassword())));
        // Role roles = roleRepository.findByName("USER").get();
        // user.setRoles(Collections.singletonList(roles));

        TPUser savedUser = userRepository.save(user);
        TPUserPrincipal principal = new TPUserPrincipal(savedUser);
        String jwtToken = jwtService.issueToken(principal);
        RefreshToken refreshToken = refreshTokenService.createAndSaveRefreshToken(savedUser);

        var tPUserDTO = TPUserRepresentation.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();

        SignInResponse signInResponse = SignInResponse
                .builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.token)
                .userPublicInfo(tPUserDTO)
                .build();

        signInResponse.add(linkTo(methodOn(AuthenticationController.class)
                .signUp(new SignUpRequest(request.getUsername(), "verySecretPassword")))
                .withSelfRel());
        signInResponse.add(linkTo(methodOn(AuthenticationController.class)
                .signIn(new SignInRequest(request.getUsername(), "verySecretPassword")))
                .withRel("signIn"));
        signInResponse.add(linkTo(methodOn(AuthenticationController.class)
                .singleSessionSignOut(new RefreshTokenRequest("verySecretRefreshToken")))
                .withRel("singleSessionLogout"));
        signInResponse.add(linkTo(methodOn(AuthenticationController.class)
                .allSessionsSignOut())
                .withRel("allSessionsLogout"));
        signInResponse.add(linkTo(methodOn(AuthenticationController.class)
                .refreshAccessToken(new RefreshTokenRequest("verySecretRefreshToken")))
                .withRel("refreshAccessToken"));

        return signInResponse;
    }

    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest refreshTokenRequest) {
        // new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
        RefreshToken refreshToken = refreshTokenService
                .findByToken(refreshTokenRequest.getRefreshToken())
                .orElseThrow(() -> new RefreshTokenNotFoundException(refreshTokenRequest.getRefreshToken()));

        if (refreshTokenService.verifyIfTokenExpiredOrRevoked(refreshToken)) {
            throw new RefreshTokenExpiredOrRevokedException(refreshTokenRequest.getRefreshToken());
        }

        TPUser owner = refreshToken.owner;
        TPUserPrincipal principal = new TPUserPrincipal(owner);
        String accessToken = jwtService.issueToken(principal);
        RefreshTokenResponse refreshTokenResponse = new RefreshTokenResponse(accessToken);
        refreshTokenResponse.add(linkTo(methodOn(AuthenticationController.class)
                .refreshAccessToken(new RefreshTokenRequest("verySecretRefreshToken")))
                .withSelfRel());
        refreshTokenResponse.add(linkTo(methodOn(AuthenticationController.class)
                .singleSessionSignOut(new RefreshTokenRequest("verySecretRefreshToken")))
                .withRel("singleSessionLogout"));
        refreshTokenResponse.add(linkTo(methodOn(AuthenticationController.class)
                .allSessionsSignOut())
                .withRel("allSessionsLogout"));
        refreshTokenResponse.add(linkTo(methodOn(AuthenticationController.class)
                .signIn(new SignInRequest("username", "verySecretPassword")))
                .withRel("signIn"));

        return refreshTokenResponse;
    }
}
