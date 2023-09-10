package org.trickyplay.trickyplayapi.users.services;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import org.trickyplay.trickyplayapi.general.exceptions.UserNotFoundException;
import org.trickyplay.trickyplayapi.users.dtos.RefreshTokenRequest;
import org.trickyplay.trickyplayapi.users.dtos.RefreshTokenResponse;
import org.trickyplay.trickyplayapi.users.dtos.RegisterRequest;
import org.trickyplay.trickyplayapi.users.dtos.SignInResponse;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.RefreshTokenRepository;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final TPUserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public SignInResponse login(String username, String password) {
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
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        // Spring Security does not care what type of Authentication implementation is set on the SecurityContext. Here, we use UsernamePasswordAuthenticationToken  - UsernamePasswordAuthenticationToken(userDetails, password, authorities).
        SecurityContextHolder.getContext().setAuthentication(authentication); // according to docs- https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html -now we set the SecurityContext on the SecurityContextHolder. Spring Security uses this information for authorization.

        // To obtain information about the authenticated principal, access the SecurityContextHolder.
        // var roles = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()

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
        // passwordEncoder.encodePassword(password, null);
        // if (authentication.isAuthenticated())
        // TPUserPrincipal principal = userDetailsService.loadUserByUsername(username);
        var principal = (TPUserPrincipal) authentication.getPrincipal();
        String accessToken = jwtService.issueToken(principal);
        var jwtToken = jwtService.issueToken(principal);
        var refreshToken = refreshTokenService.createAndSaveRefreshToken(principal.getId());
        System.out.println("Successfully authenticated. Security context contains: " + SecurityContextHolder.getContext().getAuthentication());
        return SignInResponse.builder().accessToken(jwtToken).refreshToken(refreshToken.token).build();
    }

    public void logOutOfSingleAccount(String refreshToken) {
        var storedToken = tokenRepository.findByToken(refreshToken).orElse(null);
        if (storedToken != null) {
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            SecurityContextHolder.clearContext();
        }
    }

    public void logOutOfAllAccount(Long userId) {
        Optional<TPUser> user = userRepository.findById(userId);
        refreshTokenService.revokeAllUserTokens(user.orElseThrow(() -> new UserNotFoundException(userId)));
    }

    public SignInResponse register(RegisterRequest request) {
        var user = TPUser.builder().name(request.getUsername()).password(passwordEncoder.encode(request.getPassword())).role(Role.USER).refreshTokens(null).updatedAt(LocalDateTime.now()).createdAt(LocalDateTime.now()).build();

        var savedUser = userRepository.save(user);
        var principal = new TPUserPrincipal(savedUser);
        var jwtToken = jwtService.issueToken(principal);
        var refreshToken = refreshTokenService.createAndSaveRefreshToken(savedUser);

        return SignInResponse.builder().accessToken(jwtToken).refreshToken(refreshToken.token).build();
    }

    public RefreshTokenResponse refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
//        List<String> filteredList = listOfOptionals.stream()
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .collect(Collectors.toList());
//
//        List<String> filteredList = listOfOptionals.stream()
//                .flatMap(o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty())
//                .collect(Collectors.toList());
//        List<String> filteredList = listOfOptionals.stream()
//                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
//                .collect(Collectors.toList());
//        List<String> filteredList = listOfOptionals.stream()
//                .flatMap(Optional::stream)
//                .collect(Collectors.toList());

        // new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
        return refreshTokenService.findByToken(refreshTokenRequest.getRefreshToken()).filter(refreshTokenService::verifyIfTokenExpiredOrRevoked).map(userInfo -> {
            TPUser owner = userInfo.owner;
            TPUserPrincipal principal = new TPUserPrincipal(owner);
            String accessToken = jwtService.issueToken(principal);
            return new RefreshTokenResponse(accessToken);
        }).orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }
}
