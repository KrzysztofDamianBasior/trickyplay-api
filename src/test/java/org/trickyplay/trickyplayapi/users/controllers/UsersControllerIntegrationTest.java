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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.comments.repositories.CommentRepository;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.replies.repositories.ReplyRepository;
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
class UsersControllerIntegrationTest extends BaseIntegrationTest {
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
    private CommentRepository commentRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    // getUsers tests -----------------------------------------------
    @Test
    @Transactional
    void given_3UsersSavedInDB_when_usersFeedGetEndpointIsHitWithValidParams_then_returnCorrespondingGetUsersResponse() throws Exception {
        TPUser firstTPUserStub = TPUser.builder()
                .name("UFGFirstUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("pa33w0rd"))
                .build();
        TPUser savedFirstTPUserStub = tpUserRepository.save(firstTPUserStub);
        TPUser secondTPUserStub = TPUser.builder()
                .name("UFGSecondUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode("pa33w0rd"))
                .build();
        TPUser savedSecondTPUserStub = tpUserRepository.save(secondTPUserStub);
        TPUser thirdTPUserStub = TPUser.builder()
                .name("UFGThirdUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode("pa33w0rd"))
                .build();
        TPUser savedThirdTPUserStub = tpUserRepository.save(thirdTPUserStub);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/users/feed")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
                //                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.users").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.users[0].id").value(savedFirstTPUserStub.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.users[1].id").value(savedSecondTPUserStub.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.users[2].id").value(savedThirdTPUserStub.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageNumber").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.last").value(true))
                .andReturn();
    }

    @Test
    @Transactional
    void given_emptyDB_when_usersFeedGetEndpointIsHitWithInvalidParams_then_returnBadRequestResponse() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/feed")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "-5")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
                //                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.get("/users/feed")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "0")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
                //                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.get("/users/feed")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "asdf")
                        .param("orderDirection", "Asc")
                )
                //                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.get("/users/feed")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "asdf")
                )
                //                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // getUser tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_usersGetEndpointIsHitWithValidPathVar_then_returnCorrespondingTPUserRepresentation() throws Exception {
        String username = "UGFirstUser";
        String password = "paSSw0rd";

        TPUser tPUserStub = TPUser.builder()
                .name(username)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{id}", savedTPUserStub.getId().toString())
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                //                .andDo(MockMvcResultHandlers.print())
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value(savedTPUserStub.getRole().name()))
                .andReturn();
    }

    @Test
    @Transactional
    void given_1UserSavedInDB_when_usersGetEndpointIsHitWithAbsentIdAsPathVar_then_returnNotFoundResponse() throws Exception {
        String username = "UGSecondUser";
        String password = "paSSw0rd";

        TPUser tPUserStub = TPUser.builder()
                .name(username)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{id}", "98765")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                //                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @Transactional
    void given_emptyDB_when_usersGetEndpointIsHitWithInvalidPathVar_then_returnBadRequestResponse() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{id}", "-4")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                //                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // getUserComments tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User3CommentsSavedInDB_when_usersCommentsGetEndpointIsHitWithValidPathVarAndParams_then_returnCorrespondingGetUsersResponse() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("UCGFirstUser")
                .password(passwordEncoder.encode("123ASDasd"))
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        Comment firstCommentStub = Comment.builder()
                .body("first test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        Comment secondCommentStub = Comment.builder()
                .body("second test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        Comment thirdCommentStub = Comment.builder()
                .body("third test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        TPUser savedTPUser = tpUserRepository.save(tpUserStub);
        Comment savedFirstComment = commentRepository.save(firstCommentStub);
        Comment savedSecondComment = commentRepository.save(secondCommentStub);
        Comment savedThirdComment = commentRepository.save(thirdCommentStub);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/comments", savedTPUser.getId())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comments").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.comments[0].id").value(savedFirstComment.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comments[1].id").value(savedSecondComment.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comments[2].id").value(savedThirdComment.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageNumber").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.last").value(true))
                .andReturn();
    }

    @Test
    @Transactional
    void given_emptyDB_when_usersCommentsGetEndpointIsHitWithInvalidParams_then_returnBadRequestResponse() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/comments", 0)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "-5")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/comments", 0)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "0")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/comments", 0)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "asdf")
                        .param("orderDirection", "Asc")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/comments", 0)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "fdsa")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @Transactional
    void given_emptyDB_when_usersCommentsGetEndpointIsHitWithInvalidPathVar_then_returnBadRequestResponse() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/comments", -5)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // getUserReplies tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1Comment3RepliesSavedInDB_when_usersRepliesGetEndpointIsHitWithValidPathVarAndParams_then_returnCorrespondingGetUsersResponse() throws Exception {
        TPUser tPUserStub = TPUser.builder()
                .name("URGFirstUser")
                .password(passwordEncoder.encode("123ASDasd"))
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        Comment commentStub = Comment.builder()
                .body("comment body")
                .gameName("Snake")
                .author(tPUserStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        Reply firstReplyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("first reply body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        Reply secondReplyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("second reply body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        Reply thirdReplyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("third reply body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        Long savedTPUserStubId = tpUserRepository.save(tPUserStub).getId();
        Long savedCommentStubId = commentRepository.save(commentStub).getId();
        Long savedFirstReplyStubId = replyRepository.save(firstReplyStub).getId();
        Long savedSecondReplyStubId = replyRepository.save(secondReplyStub).getId();
        Long savedThirdReplyStubId = replyRepository.save(thirdReplyStub).getId();

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/replies", savedTPUserStubId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.replies").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.replies[0].id").value(savedFirstReplyStubId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.replies[1].id").value(savedSecondReplyStubId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.replies[2].id").value(savedThirdReplyStubId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageNumber").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.last").value(true))
                .andReturn();
    }

    @Test
    @Transactional
    void given_emptyDB_when_usersRepliesGetEndpointIsHitWithInvalidParams_then_returnBadRequestResponse() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/replies", 0)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "-5")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/replies", 0)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "0")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/replies", 0)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "asdf")
                        .param("orderDirection", "Asc")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/replies", 0)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "fdsa")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @Transactional
    void given_emptyDB_when_usersRepliesGetEndpointIsHitWithInvalidPathVar_then_returnBadRequestResponse() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}/replies", -5)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ban tests -----------------------------------------------
    @Test
    @Transactional
    void given_2UsersSavedInDB_when_usersBanPatchEndpointIsHitWithValidPathVar_then_returnCorrespondingTPUserRepresentation() throws Exception {
        String firstUserName = "UBPFirstUser";
        String secondUserName = "UBPSecondUser";
        String password = "paSSw0rd";

        TPUser adminTPUserStub = TPUser.builder()
                .name(firstUserName)
                .role(Role.ADMIN)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedAdminTPUserStub = tpUserRepository.save(adminTPUserStub);
        TPUserPrincipal principal = new TPUserPrincipal(savedAdminTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        TPUser userTPUserStub = TPUser.builder()
                .name(secondUserName)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedUserTPUserStub = tpUserRepository.save(userTPUserStub);
        LocalDateTime savedTPUserGetUpdatedAt = savedUserTPUserStub.getUpdatedAt();
        Thread.sleep(5); // long startTime = System.currentTimeMillis();

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}/ban", savedUserTPUserStub.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedUserTPUserStub.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(savedUserTPUserStub.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value(Role.BANNED.name()))
                .andDo(requestResult -> {
                    String json = requestResult.getResponse().getContentAsString();
                    LocalDateTime createdAt = LocalDateTime.parse(JsonPath.parse(json).read("$.createdAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime updatedAt = LocalDateTime.parse(JsonPath.parse(json).read("$.updatedAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    assertThat(updatedAt).isAfter(savedTPUserGetUpdatedAt);
                    assertThat(createdAt).isEqualTo(savedUserTPUserStub.getCreatedAt());
                });
    }

    @Test
    @Transactional
    void given_2UsersSavedInDB_when_usersBanPatchEndpointIsHitWithAbsentIdAsPathVar_then_returnNotFoundResponse() throws Exception {
        String firstUserName = "UBPThirdUser";
        String secondUserName = "UBPFourthUser";
        String password = "paSSw0rd";

        TPUser adminTPUserStub = TPUser.builder()
                .name(firstUserName)
                .role(Role.ADMIN)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedAdminTPUserStub = tpUserRepository.save(adminTPUserStub);

        TPUser userTPUserStub = TPUser.builder()
                .name(secondUserName)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedUserTPUserStub = tpUserRepository.save(userTPUserStub);

        TPUserPrincipal principal = new TPUserPrincipal(savedAdminTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}/ban", 99999)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @Transactional
    void given_2UsersSavedInDB_when_usersBanPatchEndpointIsHitWithInvalidPathVar_then_returnBadRequestResponse() throws Exception {
        String firstUserName = "UBPFifthUser";
        String secondUserName = "UBPSixthUser";
        String password = "paSSw0rd";

        TPUser adminTPUserStub = TPUser.builder()
                .name(firstUserName)
                .role(Role.ADMIN)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedAdminTPUserStub = tpUserRepository.save(adminTPUserStub);

        TPUser userTPUserStub = TPUser.builder()
                .name(secondUserName)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedUserTPUserStub = tpUserRepository.save(userTPUserStub);

        TPUserPrincipal principal = new TPUserPrincipal(savedAdminTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}/ban", -5)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @Transactional
    void given_2UsersSavedInDB_when_usersBanPatchEndpointIsHitWithInappropriatePermissions_then_returnForbidden() throws Exception {
        String firstUserName = "UBPSeventhUser";
        String secondUserName = "UBPEightUser";
        String password = "paSSw0rd";

        TPUser adminTPUserStub = TPUser.builder()
                .name(firstUserName)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedAdminTPUserStub = tpUserRepository.save(adminTPUserStub);

        TPUser userTPUserStub = TPUser.builder()
                .name(secondUserName)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedUserTPUserStub = tpUserRepository.save(userTPUserStub);

        TPUserPrincipal principal = new TPUserPrincipal(savedAdminTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}/ban", savedUserTPUserStub.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    // unban tests -----------------------------------------------
    @Test
    @Transactional
    void given_2UsersSavedInDB_when_usersUnbanPatchEndpointIsHitWithValidPathVar_then_returnCorrespondingTPUserRepresentation() throws Exception {
        String firstUserName = "UUPFirstUser";
        String secondUserName = "UUPSecondUser";
        String password = "paSSw0rd";

        TPUser adminTPUserStub = TPUser.builder()
                .name(firstUserName)
                .role(Role.ADMIN)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedAdminTPUserStub = tpUserRepository.save(adminTPUserStub);

        TPUser userTPUserStub = TPUser.builder()
                .name(secondUserName)
                .role(Role.BANNED)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedUserTPUserStub = tpUserRepository.save(userTPUserStub);
        LocalDateTime savedTPUserGetUpdatedAt = savedUserTPUserStub.getUpdatedAt();
        Thread.sleep(5); // long startTime = System.currentTimeMillis();

        TPUserPrincipal principal = new TPUserPrincipal(savedAdminTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}/unban", savedUserTPUserStub.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedUserTPUserStub.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(savedUserTPUserStub.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value(Role.USER.name()))
                .andDo(requestResult -> {
                    String json = requestResult.getResponse().getContentAsString();
                    LocalDateTime createdAt = LocalDateTime.parse(JsonPath.parse(json).read("$.createdAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime updatedAt = LocalDateTime.parse(JsonPath.parse(json).read("$.updatedAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    assertThat(updatedAt).isAfter(savedTPUserGetUpdatedAt);
                    assertThat(createdAt).isEqualTo(savedUserTPUserStub.getCreatedAt());
                });
    }

    @Test
    @Transactional
    void given_2UsersSavedInDB_when_usersUnbanPatchEndpointIsHitWithAbsentIdAsPathVar_then_returnNotFoundResponse() throws Exception {
        String firstUserName = "UUPThirdUser";
        String secondUserName = "UUPFourthUser";
        String password = "paSSw0rd";

        TPUser adminTPUserStub = TPUser.builder()
                .name(firstUserName)
                .role(Role.ADMIN)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedAdminTPUserStub = tpUserRepository.save(adminTPUserStub);

        TPUser userTPUserStub = TPUser.builder()
                .name(secondUserName)
                .role(Role.BANNED)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedUserTPUserStub = tpUserRepository.save(userTPUserStub);

        TPUserPrincipal principal = new TPUserPrincipal(savedAdminTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}/unban", 99999)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @Transactional
    void given_2UsersSavedInDB_when_usersUnbanPatchEndpointIsHitWithInvalidPathVar_then_returnBadRequestResponse() throws Exception {
        String firstUserName = "UUPFifthUser";
        String secondUserName = "UUPSixthUser";
        String password = "paSSw0rd";

        TPUser adminTPUserStub = TPUser.builder()
                .name(firstUserName)
                .role(Role.ADMIN)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedAdminTPUserStub = tpUserRepository.save(adminTPUserStub);

        TPUser userTPUserStub = TPUser.builder()
                .name(secondUserName)
                .role(Role.BANNED)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedUserTPUserStub = tpUserRepository.save(userTPUserStub);

        TPUserPrincipal principal = new TPUserPrincipal(savedAdminTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}/unban", -5)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @Transactional
    void given_2UsersSavedInDB_when_usersUnbanPatchEndpointIsHitWithInappropriatePermissions_then_returnForbidden() throws Exception {
        String firstUserName = "UUPSeventhUser";
        String secondUserName = "UUPEightUser";
        String password = "paSSw0rd";

        TPUser adminTPUserStub = TPUser.builder()
                .name(firstUserName)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedAdminTPUserStub = tpUserRepository.save(adminTPUserStub);

        TPUser userTPUserStub = TPUser.builder()
                .name(secondUserName)
                .role(Role.BANNED)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedUserTPUserStub = tpUserRepository.save(userTPUserStub);

        TPUserPrincipal principal = new TPUserPrincipal(savedAdminTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}/unban", savedUserTPUserStub.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    // grantAdminPermissions tests -----------------------------------------------
    @Test
    @Transactional
    void given_2UsersSavedInDB_when_usersGrantAdminPermissionsPatchEndpointIsHitWithValidPathVar_then_returnCorrespondingTPUserRepresentation() throws Exception {
        String firstUserName = "UGAPFirstUser";
        String secondUserName = "UGAPSecondUser";
        String password = "paSSw0rd";

        TPUser adminTPUserStub = TPUser.builder()
                .name(firstUserName)
                .role(Role.ADMIN)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedAdminTPUserStub = tpUserRepository.save(adminTPUserStub);
        TPUserPrincipal principal = new TPUserPrincipal(savedAdminTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        TPUser userTPUserStub = TPUser.builder()
                .name(secondUserName)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedUserTPUserStub = tpUserRepository.save(userTPUserStub);
        LocalDateTime savedTPUserGetUpdatedAt = savedUserTPUserStub.getUpdatedAt();
        Thread.sleep(5); // long startTime = System.currentTimeMillis();

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}/grant-admin-permissions", savedUserTPUserStub.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedUserTPUserStub.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(savedUserTPUserStub.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value(Role.ADMIN.name()))
                .andDo(requestResult -> {
                    String json = requestResult.getResponse().getContentAsString();
                    LocalDateTime createdAt = LocalDateTime.parse(JsonPath.parse(json).read("$.createdAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime updatedAt = LocalDateTime.parse(JsonPath.parse(json).read("$.updatedAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    assertThat(updatedAt).isAfter(savedTPUserGetUpdatedAt);
                    assertThat(createdAt).isEqualTo(savedUserTPUserStub.getCreatedAt());
                });
    }

    @Test
    @Transactional
    void given_2UsersSavedInDB_when_usersGrantAdminPermissionsPatchEndpointIsHitWithAbsentIdAsPathVar_then_returnNotFoundResponse() throws Exception {
        String firstUserName = "UGAPThirdUser";
        String secondUserName = "UGAPFourthUser";
        String password = "paSSw0rd";

        TPUser adminTPUserStub = TPUser.builder()
                .name(firstUserName)
                .role(Role.ADMIN)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedAdminTPUserStub = tpUserRepository.save(adminTPUserStub);

        TPUser userTPUserStub = TPUser.builder()
                .name(secondUserName)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedUserTPUserStub = tpUserRepository.save(userTPUserStub);

        TPUserPrincipal principal = new TPUserPrincipal(savedAdminTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}/grant-admin-permissions", 99999)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @Transactional
    void given_2UsersSavedInDB_when_usersGrantAdminPermissionsPatchEndpointIsHitWithInvalidPathVar_then_returnBadRequestResponse() throws Exception {
        String firstUserName = "UGAPFifthUser";
        String secondUserName = "UGAPSixthUser";
        String password = "paSSw0rd";

        TPUser adminTPUserStub = TPUser.builder()
                .name(firstUserName)
                .role(Role.ADMIN)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedAdminTPUserStub = tpUserRepository.save(adminTPUserStub);

        TPUser userTPUserStub = TPUser.builder()
                .name(secondUserName)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedUserTPUserStub = tpUserRepository.save(userTPUserStub);

        TPUserPrincipal principal = new TPUserPrincipal(savedAdminTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}/grant-admin-permissions", -5)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @Transactional
    void given_2UsersSavedInDB_when_usersGrantAdminPermissionsPatchEndpointIsHitWithInappropriatePermissions_then_returnForbidden() throws Exception {
        String firstUserName = "UGAPSeventhUser";
        String secondUserName = "UGAPEightUser";
        String password = "paSSw0rd";

        TPUser adminTPUserStub = TPUser.builder()
                .name(firstUserName)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedAdminTPUserStub = tpUserRepository.save(adminTPUserStub);
        TPUserPrincipal principal = new TPUserPrincipal(savedAdminTPUserStub);
        var accessToken = jwtService.issueToken(principal);

        TPUser userTPUserStub = TPUser.builder()
                .name(secondUserName)
                .role(Role.USER)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .password(passwordEncoder.encode(password))
                .build();
        TPUser savedUserTPUserStub = tpUserRepository.save(userTPUserStub);

        System.out.println(savedUserTPUserStub.getId());
        System.out.println(savedAdminTPUserStub.getId());
        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}/grant-admin-permissions", savedUserTPUserStub.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}