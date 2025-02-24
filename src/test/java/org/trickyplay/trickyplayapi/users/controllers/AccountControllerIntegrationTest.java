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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.users.dtos.EditAccountRequest;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;
import org.trickyplay.trickyplayapi.users.services.JwtService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIntegrationTest extends BaseIntegrationTest {
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

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    // getMyAccount tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_accountGetEndpointIsHitByAuthenticatedUser_then_returnCorrespondingTPUserRepresentation() throws Exception {
        TPUser tPUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);

        TPUserPrincipal principal = new TPUserPrincipal(savedTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        mockMvc.perform(MockMvcRequestBuilders.get("/account")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedTPUserStub.getId()))
                .andDo(requestResult -> {
                    String json = requestResult.getResponse().getContentAsString();
                    LocalDateTime createdAt = LocalDateTime.parse(JsonPath.parse(json).read("$.createdAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime updatedAt = LocalDateTime.parse(JsonPath.parse(json).read("$.updatedAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    assertThat(createdAt).isEqualTo(savedTPUserStub.getCreatedAt());
                    assertThat(updatedAt).isEqualTo(savedTPUserStub.getUpdatedAt());
                })
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(savedTPUserStub.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value(savedTPUserStub.getRole().name()));
    }

    @Test
    @Transactional
    void given_emptyDB_when_accountGetEndpointIsHitByUnauthenticatedUser_then_returnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/account"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    // deleteAccount tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_accountDeleteEndpointIsHitByAuthenticatedUser_then_returnCorrespondingDeleteAccountResponse() throws Exception {
        TPUser tPUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);

        TPUserPrincipal principal = new TPUserPrincipal(savedTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        mockMvc.perform(MockMvcRequestBuilders.delete("/account")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("The account for user with id: " + savedTPUserStub.getId() + " has been removed"));
    }

    @Test
    @Transactional
    void given_emptyDB_when_accountDeleteEndpointIsHitByUnauthenticatedUser_then_returnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/account"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    // editAccount tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_accountPatchEndpointIsHitByAuthenticatedUser_then_returnCorrespondingTPUserRepresentation() throws Exception {
        TPUser tPUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);
        LocalDateTime savedTPUserGetUpdatedAt = savedTPUserStub.getUpdatedAt();
        Thread.sleep(5); // long startTime = System.currentTimeMillis();

        // LocalDateTime localNow = LocalDateTime.now();
        // setting UTC as the timezone
        // ZonedDateTime zonedUTC = localNow.atZone(ZoneId.of("UTC"));

        TPUserPrincipal principal = new TPUserPrincipal(savedTPUserStub);
        var accessToken = jwtService.issueToken(principal);
        EditAccountRequest editAccountRequest = new EditAccountRequest("newUsername", "newPa22word");

        mockMvc.perform(MockMvcRequestBuilders.patch("/account")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
//                        .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editAccountRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedTPUserStub.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(savedTPUserStub.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value(savedTPUserStub.getRole().name()))
                .andDo(requestResult -> {
                    String json = requestResult.getResponse().getContentAsString();
                    LocalDateTime createdAt = LocalDateTime.parse(JsonPath.parse(json).read("$.createdAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime updatedAt = LocalDateTime.parse(JsonPath.parse(json).read("$.updatedAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    assertThat(updatedAt).isAfter(savedTPUserGetUpdatedAt);
                    assertThat(createdAt).isEqualTo(savedTPUserStub.getCreatedAt());
                });
    }

    @Test
    @Transactional
    void given_1UserSavedInDB_when_accountPatchEndpointIsHitWithInvalidRequestBodyByAuthenticatedUser_then_returnBadRequestResponse() throws Exception {
        TPUser tPUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);

        TPUserPrincipal principal = new TPUserPrincipal(savedTPUserStub);
        var accessToken = jwtService.issueToken(principal);
        EditAccountRequest editAccountRequest = new EditAccountRequest("12345678901234567", "12");

        Thread.sleep(5); // long startTime = System.currentTimeMillis();
        mockMvc.perform(MockMvcRequestBuilders.patch("/account")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editAccountRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @Transactional
    void given_emptyDB_when_accountPatchEndpointIsHitByUnauthenticatedUser_then_returnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/account"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}