package org.trickyplay.trickyplayapi.users.controllers;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final TPUserRepository userRepository;

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

        return authenticationService.signIn(signInDto); // attempt login
    }

    // As for using underscores, hyphens, or nothing, the best thing for search engine optimization is to use hyphens, then each "word" in the URL is considered a "term" by the search engines. Bing treats underscores and hyphens the same way, but Google (at least as recently as I can find) treats words separated by hyphens as separate terms, but combines words separated by underscores into a single term.
    @PostMapping("/sign-up")
    public ResponseEntity<SignInResponse> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByName(signUpRequest.getUsername())) {
            throw new NameTakenException("This username is already taken");
        }
        var signInResponse = authenticationService.signUp(signUpRequest);
        URI userURI = URI.create("/users/" + signInResponse.getUserPublicInfo().getId());
        return ResponseEntity.created(userURI).body(signInResponse);
    }

    @GetMapping("/refresh-access-token")
    public RefreshTokenResponse refreshAccessToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authenticationService.refreshAccessToken(refreshTokenRequest);
    }

    // Logout ensure that all sensitive information is removed or invalidated once user performs the logout.
    @DeleteMapping("/single-session-logout")
    public SignOutResponse singleSessionLogout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authenticationService.singleSessionLogout(refreshTokenRequest.getRefreshToken());
    }

    @DeleteMapping("/all-sessions-logout")
    @PreAuthorize("isAuthenticated()") // SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
    public SignOutResponse allSessionsLogout(@AuthenticationPrincipal TPUserPrincipal user) {
//        Object principal = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        if (principal instanceof TPUserPrincipal) {
//            String username = ((TPUserPrincipal) principal).getUsername();
//            long principalId = ((TPUserPrincipal) principal).getId();
//        } else {
//            String username = principal.toString();
//        }

        return authenticationService.allSessionsLogout(user.getId());
    }
}
