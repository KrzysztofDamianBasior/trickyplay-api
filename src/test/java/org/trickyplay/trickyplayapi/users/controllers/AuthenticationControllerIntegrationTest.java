package org.trickyplay.trickyplayapi.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.users.dtos.RefreshTokenRequest;
import org.trickyplay.trickyplayapi.users.dtos.SignInRequest;
import org.trickyplay.trickyplayapi.users.dtos.SignUpRequest;
import org.trickyplay.trickyplayapi.users.entities.RefreshToken;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.RefreshTokenRepository;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;
import org.trickyplay.trickyplayapi.users.services.JwtService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TPUserRepository tpUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    // signIn tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_authSignInPostEndpointIsHitWithValidRequestBody_then_returnCorrespondingSignInResponse() throws Exception {
        String username = "authSIUser";
        String password = "paSSw0rd";
        TPUser tPUserStub = TPUser.builder()
                .name(username)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);

        SignInRequest signInRequest = new SignInRequest(username, password);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/sign-in")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userPublicInfo.id").value(savedTPUserStub.getId()))
                .andDo(requestResult -> {
                    String json = requestResult.getResponse().getContentAsString();
                    LocalDateTime createdAt = LocalDateTime.parse(JsonPath.parse(json).read("$.userPublicInfo.createdAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime updatedAt = LocalDateTime.parse(JsonPath.parse(json).read("$.userPublicInfo.updatedAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    assertThat(createdAt).isEqualTo(savedTPUserStub.getCreatedAt());
                    assertThat(updatedAt).isEqualTo(savedTPUserStub.getUpdatedAt());
                })
                .andExpect(MockMvcResultMatchers.jsonPath("$.userPublicInfo.name").value(savedTPUserStub.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userPublicInfo.role").value(savedTPUserStub.getRole().name()));
    }

    @Test
    @Transactional
    void given_emptyDB_when_authSignInPostEndpointIsHitWithInvalidRequestBody_then_returnBadRequestResponse() throws Exception {
        SignInRequest signInRequest = new SignInRequest("1", "123");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/sign-in")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // signUp tests -----------------------------------------------
    @Test
    @Transactional
    void given_emptyDB_when_authSignUpPostEndpointIsHitWithValidRequestBody_then_returnCorrespondingSignInResponse() throws Exception {
        String username = "authSUPUsername";
        SignUpRequest signUpRequest = new SignUpRequest(username, "123pasSSword");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/sign-up")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userPublicInfo.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userPublicInfo.updatedAt").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userPublicInfo.createdAt").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userPublicInfo.name").value(username))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userPublicInfo.role").value(Role.USER.name()));
    }

    @Test
    @Transactional
    void given_emptyDB_when_authSignUpPostEndpointIsHitWithInvalidRequestBody_then_returnBadRequestResponse() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest("1", "123");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/sign-up")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // refreshAccessToken tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_authRefreshAccessTokenPostEndpointIsHitWithValidRequestBody_then_returnCorrespondingSignInResponse() throws Exception {
        String username = "authSIUser";
        String password = "paSSw0rd";
        TPUser tPUserStub = TPUser.builder()
                .name(username)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);

        MvcResult login = mockMvc.perform(MockMvcRequestBuilders.post("/auth/sign-in")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
//        String authToken = login.getResponse().getHeader("Authorization");
        String authToken = JsonPath.parse(login.getResponse().getContentAsString()).read("$.accessToken").toString();
        String refreshToken = JsonPath.parse(login.getResponse().getContentAsString()).read("$.refreshToken").toString();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/refresh-access-token")
                        .header("Authorization", authToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}")
                )
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @Transactional
    void given_1User1RefreshTokenSavedInDB_when_authRefreshAccessTokenPostEndpointIsHitWithInvalidRequestBody_then_returnBadRequestResponse() throws Exception {
        String username = "authSIUser";
        String password = "paSSw0rd";
        TPUser tPUserStub = TPUser.builder()
                .name(username)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .token("12345")
                .owner(savedTPUserStub)
                .expiryDate(Instant.now().plusMillis(10000))
                .revoked(false)
                .build();
        RefreshToken savedRefreshTokenStub = refreshTokenRepository.save(refreshTokenStub);

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/refresh-access-token")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @Transactional
    void given_emptyDB_when_authRefreshAccessTokenPostEndpointIsHitWithRefreshTokenMissingInDB_then_returnRefreshTokenNotFound() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("12345");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/refresh-access-token")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    // singleSessionSignOut tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1RefreshTokenSavedInDB_when_authSingleSessionSignOutPostEndpointIsHitWithValidRequestBody_then_returnCorrespondingSignOutResponse() throws Exception {
        String username = "authSIUser";
        String password = "paSSw0rd";
        TPUser tPUserStub = TPUser.builder()
                .name(username)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .token("12345")
                .owner(savedTPUserStub)
                .expiryDate(Instant.now().plusMillis(10000))
                .revoked(false)
                .build();
        RefreshToken savedRefreshTokenStub = refreshTokenRepository.save(refreshTokenStub);

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(refreshTokenStub.getToken());

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/single-session-sign-out")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfRefreshTokensRemoved").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("successfully signed out"));
    }

    @Test
    @Transactional
    void given_1User1RefreshTokenSavedInDB_when_authSingleSessionSignOutPostEndpointIsHitWithInvalidRequestBody_then_returnBadRequestResponse() throws Exception {
        String username = "authSIUser";
        String password = "paSSw0rd";
        TPUser tPUserStub = TPUser.builder()
                .name(username)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);
        RefreshToken refreshTokenStub = RefreshToken.builder()
                .token("12345")
                .owner(savedTPUserStub)
                .expiryDate(Instant.now().plusMillis(10000))
                .revoked(false)
                .build();
        RefreshToken savedRefreshTokenStub = refreshTokenRepository.save(refreshTokenStub);

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/single-session-sign-out")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // allSessionsSignOut tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User3RefreshTokenSavedInDB_when_authAllSessionsSignOutPostEndpointIsHitByAuthenticatedUser_then_returnCorrespondingSignOutResponse() throws Exception {
        String username = "authSIUser";
        String password = "paSSw0rd";
        TPUser tPUserStub = TPUser.builder()
                .name(username)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);
        RefreshToken firstRefreshTokenStub = RefreshToken.builder()
                .token("12345")
                .owner(savedTPUserStub)
                .expiryDate(Instant.now().plusMillis(10000))
                .revoked(false)
                .build();
        RefreshToken savedRefreshTokenStub = refreshTokenRepository.save(firstRefreshTokenStub);
        RefreshToken secondRefreshTokenStub = RefreshToken.builder()
                .token("23456")
                .owner(savedTPUserStub)
                .expiryDate(Instant.now().plusMillis(10000))
                .revoked(false)
                .build();
        RefreshToken secondSavedRefreshTokenStub = refreshTokenRepository.save(secondRefreshTokenStub);
        RefreshToken thirdRefreshTokenStub = RefreshToken.builder()
                .token("34567")
                .owner(savedTPUserStub)
                .expiryDate(Instant.now().plusMillis(10000))
                .revoked(false)
                .build();
        RefreshToken thirdSavedRefreshTokenStub = refreshTokenRepository.save(thirdRefreshTokenStub);
        TPUserPrincipal principal = new TPUserPrincipal(savedTPUserStub);
        var accessToken = jwtService.issueToken(principal);
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/all-sessions-sign-out")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfRefreshTokensRemoved").value("3"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("successfully signed out of all sessions"));
    }

    @Test
    @Transactional
    void given_emptyDB_when_authAllSessionsSignOutPostEndpointIsHitByUnauthenticatedUser_then_returnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/all-sessions-sign-out"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}
