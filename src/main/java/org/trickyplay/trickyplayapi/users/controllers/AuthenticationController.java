package org.trickyplay.trickyplayapi.users.controllers;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    // @PostMapping("/auth/login")
    // public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request) {
    // public JwtResponse login(@RequestBody AuthRequest authRequest) {
    // In case the credential is invalid, a BadCredentialsException is thrown and the API returns HTTP status 401
    // (Unauthorized). If valid, it uses the AuthenticationService class to generate a new access token, which is then attached
    // to the response object of type AuthResponse.
    //
    // The value of the accessToken field is the generated JWT, which can be used in subsequent calls that access
    // secure REST APIs. If you provide wrong username and password, it will return HTTP 401 (Unauthorized). If
    // you specify invalid email format or the password is too short or too long (controlled by the @Email and @Length
    // validation annotations), you will get HTTP 400 (Bad Request) status.


//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (principal instanceof UserDetails) {
//            String username = ((UserDetails) principal).getUsername();
//        } else {
//            String username = principal.toString();
//        }
}

//    @PostMapping("/refreshToken")
