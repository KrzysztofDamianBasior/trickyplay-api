package org.trickyplay.trickyplayapi.users.controllers;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.trickyplay.trickyplayapi.general.exceptions.NameTakenException;
import org.trickyplay.trickyplayapi.users.dtos.*;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;
import org.trickyplay.trickyplayapi.users.services.AuthenticationService;

import java.net.URI;

@Validated // validate parameters that are passed into a method
@RestController
@RequestMapping("auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final TPUserRepository userRepository;

    private final Counter usernameTakenCounter;
    private final Counter tokenRefreshedCounter;
    private final Counter signedOutCounter;
    private final Counter signedInCounter;
    private final Counter signedUpCounter;

    public AuthenticationController(MeterRegistry registry, AuthenticationService authenticationService, TPUserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;

        usernameTakenCounter = registry.counter("controllers.username-taken");
        tokenRefreshedCounter = registry.counter("controllers.token-refreshed");
        signedOutCounter = registry.counter("controllers.signed-out");
        signedInCounter = registry.counter("controllers.signed-in");
        signedUpCounter = registry.counter("controllers.signed-up");
    }

    // Microsoft Style Guide- https://learn.microsoft.com/en-us/style-guide/a-z-word-list-term-collections/s/sign-in-sign-out
    // Use sign in to describe starting a session on a computer, a device, a network, an app, or anywhere a username and password are required. Use sign out to refer to ending a session. Don't use log in, login, log into, log on, logon, log onto, log off, log out, logout, sign into, signin, signoff, sign off, or sign on unless these terms appear in the UI (and you're writing instructions). The verb form is two words, sign in or sign out. Avoid using as a noun or adjective
    @PostMapping("/sign-in")
    @PreAuthorize("permitAll()")
    public SignInResponse signIn(@Valid @RequestBody SignInRequest signInDto) {
        // In case the credential is invalid, a BadCredentialsException is thrown and the API returns HTTP status 401
        // (Unauthorized). If valid, it uses the AuthenticationService class to generate a new access token, which is then attached
        // to the response object of type SignInResponse.
        //
        // The value of the accessToken field is the generated JWT, which can be used in subsequent calls that access
        // secure REST APIs. If you provide wrong username and password, it will return HTTP 401 (Unauthorized). If
        // you specify invalid username format or the password is too short or too long (controlled by the @Pattern and @Length
        // validation annotations), you will get HTTP 400 (Bad Request) status.
        signedInCounter.increment();

        return authenticationService.signIn(signInDto); // attempt login
    }

    // As for using underscores, hyphens, or nothing, the best thing for search engine optimization is to use hyphens, then each "word" in the URL is considered a "term" by the search engines. Bing treats underscores and hyphens the same way, but Google (at least as recently as I can find) treats words separated by hyphens as separate terms, but combines words separated by underscores into a single term.
    //    @CookieValue(name = "JSESSIONID") String sessionId
    @PostMapping("/sign-up")
    public ResponseEntity<SignInResponse> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByName(signUpRequest.getUsername())) {
            usernameTakenCounter.increment();
            throw new NameTakenException("This username is already taken");
        }

        var signInResponse = authenticationService.signUp(signUpRequest);

        signedUpCounter.increment();

        URI userURI = URI.create("/users/" + signInResponse.getUserPublicInfo().getId());
        return ResponseEntity.created(userURI).body(signInResponse);
    }

    // Intuitively, a get request is the right solution for retrieving a new refresh token, however, this operation requires a secret password that should not be sent in the URL, and a get request does not allow the request body to have a semantic meaning
    // Moreover, the problem in sending the secret in the get request body is the javascript engine. According to the documentation and the spec XMLHttpRequest ignores the body of the request in case the method is GET. If you perform a request in Chrome/Electron with XMLHttpRequest and you try to put a json body in the send method this just gets ignored. Using fetch which is the modern replacement for XMLHtppRequest also seems to fail in Chrome/Electron.
    // From the HTTP 1.1 2014 Spec: A payload within a GET request message has no defined semantics; sending a payload body on a GET request might cause some existing implementations to reject the request.
    @PostMapping("/refresh-access-token")
    @Timed("refresh-token-timer")
    public RefreshTokenResponse refreshAccessToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        RefreshTokenResponse refreshTokenResponse = authenticationService.refreshAccessToken(refreshTokenRequest);

        tokenRefreshedCounter.increment();

        return refreshTokenResponse;
    }

    // Logout ensure that all sensitive information is removed or invalidated once user performs the logout.
    // For reasons similar to why requesting a refresh-token requires a post request rather than a get request, a logout request must be a post request rather than a delete request. Secret information should not be saved in the url. If sending in the request body is required, use the post or put methods
    @PostMapping("/single-session-sign-out")
    public SignOutResponse singleSessionSignOut(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        signedOutCounter.increment();

        return authenticationService.singleSessionLogout(refreshTokenRequest.getRefreshToken());
    }

    // For consistency with single session logout, multi-session logout should also be handled by a post request rather than a delete request
    @PostMapping("/all-sessions-sign-out")
    @PreAuthorize("isAuthenticated()") // SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
    public SignOutResponse allSessionsSignOut(
//            @AuthenticationPrincipal TPUserPrincipal user // hateoas methodOn does not allow the controller to accept principal as an argument
    ) {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        long principalId = ((TPUserPrincipal) principal).getId();

        signedOutCounter.increment();

        return authenticationService.allSessionsLogout(principalId);
    }
}
