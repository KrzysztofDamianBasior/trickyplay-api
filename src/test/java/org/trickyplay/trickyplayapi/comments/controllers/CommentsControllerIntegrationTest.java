package org.trickyplay.trickyplayapi.comments.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.comments.dtos.*;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.comments.repositories.CommentRepository;
import org.trickyplay.trickyplayapi.replies.repositories.ReplyRepository;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;
import org.trickyplay.trickyplayapi.users.services.JwtService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//    use the following naming convention: Given_Preconditions_When_StateUnderTest_Then_ExpectedBehavior — Behavior-Driven Development (BDD)

@SpringBootTest
@AutoConfigureMockMvc
class CommentsControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CommentRepository commentsRepository;

    @Autowired
    private TPUserRepository tpUserRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private MockMvc mockMvc;

//    @Autowired
//    private WebApplicationContext webApplicationContext;
//
//    @Before
//    public void setUp() {
//        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
//    }

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

    // getCommentsByGameName tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User3CommentsSavedInDB_when_commentsFeedGetEndpointIsHitWithValidParams_then_returnCorrespondingGetCommentsResponse() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .password(passwordEncoder.encode("123ASDasd"))
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        Comment firstCommentStub = Comment.builder()
                .body("first test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Comment secondCommentStub = Comment.builder()
                .body("second test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Comment thirdCommentStub = Comment.builder()
                .body("third test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        tpUserRepository.save(tpUserStub);
        Long firstCommentStubId = commentsRepository.save(firstCommentStub).getId();
        Long secondCommentStubId = commentsRepository.save(secondCommentStub).getId();
        commentsRepository.saveAndFlush(thirdCommentStub);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/comments/feed")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("gameName", "Snake")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comments").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.comments[0].id").value(firstCommentStubId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comments[1].id").value(secondCommentStubId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageNumber").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.last").value(true))
                .andReturn();
    }

    @Test
    @Transactional
    void given_1User3CommentsSavedInDB_when_commentsFeedGetEndpointIsHitWithInvalidParams_then_returnBadRequestResponse() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();

        Comment firstCommentStub = Comment.builder()
                .body("first test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Comment secondCommentStub = Comment.builder()
                .body("second test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Comment thirdCommentStub = Comment.builder()
                .body("third test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        tpUserRepository.save(tpUserStub);
        Long firstCommentStubId = commentsRepository.save(firstCommentStub).getId();
        Long secondCommentStubId = commentsRepository.save(secondCommentStub).getId();
        commentsRepository.saveAndFlush(thirdCommentStub);

        mockMvc.perform(MockMvcRequestBuilders.get("/comments/feed")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("gameName", "badGameName")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.get("/comments/feed")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("gameName", "Snake")
                        .param("pageNumber", "invalidPageNumber")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.get("/comments/feed")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("gameName", "Snake")
                        .param("pageNumber", "0")
                        .param("pageSize", "-1")
                        .param("sortBy", "id")
                        .param("orderDirection", "Asc")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.get("/comments/feed")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("gameName", "Snake")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "test")
                        .param("orderDirection", "Asc")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.get("/comments/feed")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("gameName", "Snake")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("orderDirection", "badDirection")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // getSingleComment tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1CommentSavedInDB_when_commentsGetEndpointIsHitWithValidPathVar_then_returnCorrespondingCommentRepresentation() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();

        Comment commentStub = Comment.builder()
                .body("first test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TPUser savedTPUserStub = tpUserRepository.save(tpUserStub);
        Comment savedCommentStub = commentsRepository.save(commentStub);

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/comments/{id}", savedCommentStub.getId())
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(commentStub.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gameName").value(commentStub.getGameName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").value(commentStub.getBody()))
                .andDo(requestResult -> {
                    String json = requestResult.getResponse().getContentAsString();
                    LocalDateTime createdAt = LocalDateTime.parse(JsonPath.parse(json).read("$.createdAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime updatedAt = LocalDateTime.parse(JsonPath.parse(json).read("$.updatedAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    assertThat(createdAt).isEqualTo(savedCommentStub.getCreatedAt());
                    assertThat(updatedAt).isEqualTo(savedCommentStub.getUpdatedAt());
                })
                .andReturn();
    }

    @Test
    @Transactional
    void given_1User1CommentSavedInDB_when_commentsGetEndpointIsHitWithAbsentIdAsPathVar_then_returnNotFoundResponse() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();

        Comment commentStub = Comment.builder()
                .body("first test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        tpUserRepository.save(tpUserStub);
        commentsRepository.saveAndFlush(commentStub);

        Long idOfCommentNotPresentInTheDatabase = 123456789L;

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/comments/{id}", idOfCommentNotPresentInTheDatabase)
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    @Test
    @Transactional
    void given_1User1CommentSavedInDB_when_commentsGetEndpointIsHitWithInvalidPathVar_then_returnBadRequestResponse() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();

        Comment commentStub = Comment.builder()
                .body("first test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        tpUserRepository.save(tpUserStub);
        commentsRepository.saveAndFlush(commentStub);

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/comments/{id}", "-2")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    // addComment tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_commentsPostEndpointIsHitWithValidRequestBody_then_returnCorrespondingCommentRepresentation() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        Long tpUserStubId = tpUserRepository.save(tpUserStub).getId();

        TPUserPrincipal principal = TPUserPrincipal.builder()
                .id(tpUserStubId)
                .name(tpUserStub.getName())
                .password(null)
                .role(Role.USER.name())
                .createdAt(tpUserStub.getCreatedAt().toString())
                .updatedAt(tpUserStub.getUpdatedAt().toString())
                .build();
        var accessToken = jwtService.issueToken(principal);

        AddCommentRequest addCommentRequest = AddCommentRequest.builder()
                .body("123456789")
                .gameName("Snake")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/comments")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addCommentRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.gameName").value(addCommentRequest.getGameName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.updatedAt").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").value(addCommentRequest.getBody()));
    }

    @Test
    @Transactional
    void given_1UserSavedInDB_when_commentsPostEndpointIsHitWithInvalidRequestBody_then_returnBadRequestResponse() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        Long tpUserStubId = tpUserRepository.save(tpUserStub).getId();

        TPUserPrincipal principal = TPUserPrincipal.builder()
                .id(tpUserStubId)
                .name(tpUserStub.getName())
                .password(null)
                .role(Role.USER.name())
                .createdAt(tpUserStub.getCreatedAt().toString())
                .updatedAt(tpUserStub.getUpdatedAt().toString())
                .build();
        var accessToken = jwtService.issueToken(principal);

        AddCommentRequest addCommentRequest = AddCommentRequest.builder()
                .body("")
                .gameName("InvalidGameName")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/comments")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addCommentRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // editComment tests ----------------------------------------------------
    @Test
    @Transactional
    void given_1Comment1UserSavedInDB_when_commentsPatchEndpointIsHitWithValidBothRequestBodyAndPathVar_then_returnCorrespondingCommentRepresentation() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        Long tpUserStubId = tpUserRepository.save(tpUserStub).getId();

        TPUserPrincipal principal = TPUserPrincipal.builder()
                .id(tpUserStubId)
                .name(tpUserStub.getName())
                .password(null)
                .role(Role.USER.name())
                .createdAt(tpUserStub.getCreatedAt().toString())
                .updatedAt(tpUserStub.getUpdatedAt().toString())
                .build();
        var accessToken = jwtService.issueToken(principal);

        Comment commentStub = Comment.builder()
                .body("first test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Long commentStubId = commentsRepository.save(commentStub).getId();

        EditCommentRequest editCommentRequest = EditCommentRequest.builder()
                .newCommentBody("newCommentBody")
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.patch("/comments/{id}", commentStubId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(editCommentRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        response.andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(commentStubId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").value(editCommentRequest.getNewCommentBody()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gameName").value(commentStub.getGameName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.updatedAt").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.author.name").value(commentStub.getAuthor().getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.author.id").value(commentStub.getAuthor().getId()));
    }

    @Test
    @Transactional
    void given_1Comment1UserSavedInDB_when_commentsPatchEndpointIsHitWithInvalidRequestBodyOrPathVar_then_returnBadRequestResponse() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        Long tpUserStubId = tpUserRepository.save(tpUserStub).getId();

        TPUserPrincipal principal = TPUserPrincipal.builder()
                .id(tpUserStubId)
                .name(tpUserStub.getName())
                .password(null)
                .role(Role.USER.name())
                .createdAt(tpUserStub.getCreatedAt().toString())
                .updatedAt(tpUserStub.getUpdatedAt().toString())
                .build();
        var accessToken = jwtService.issueToken(principal);

        Comment commentStub = Comment.builder()
                .body("first test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Long commentStubId = commentsRepository.save(commentStub).getId();

        mockMvc.perform(MockMvcRequestBuilders.patch("/comments/{id}", "0")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(EditCommentRequest.builder().newCommentBody("").build())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.patch("/comments/{id}", "-1")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(EditCommentRequest.builder().newCommentBody("test").build())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.patch("/comments/{id}", "0")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(EditCommentRequest.builder().newCommentBody(
                                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" +
                                        "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" +
                                        "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" +
                                        "1"
                        ).build())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // deleteComment tests --------------------------------------------------
    @Test
    @Transactional
    void given_1Comment1UserSavedInDB_when_commentsDeleteEndpointIsHitWithValidPathVar_then_returnCorrespondingDeleteCommentResponse() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        Long tpUserStubId = tpUserRepository.save(tpUserStub).getId();

        TPUserPrincipal principal = TPUserPrincipal.builder()
                .id(tpUserStubId)
                .name(tpUserStub.getName())
                .password(null)
                .role(Role.USER.name())
                .createdAt(tpUserStub.getCreatedAt().toString())
                .updatedAt(tpUserStub.getUpdatedAt().toString())
                .build();
        var accessToken = jwtService.issueToken(principal);

        Comment commentStub = Comment.builder()
                .body("first test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Long commentStubId = commentsRepository.save(commentStub).getId();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.delete("/comments/{id}", commentStubId)
                .header("Authorization", "Bearer " + accessToken)
        );

        response.andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Comment successfully removed"));
    }

    @Test
    @Transactional
    void given_1Comment1UserSavedInDB_when_commentsDeleteIsHitWithInvalidPathVar_then_returnBadRequestResponse() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        Long tpUserStubId = tpUserRepository.save(tpUserStub).getId();

        TPUserPrincipal principal = TPUserPrincipal.builder()
                .id(tpUserStubId)
                .name(tpUserStub.getName())
                .password(null)
                .role(Role.USER.name())
                .createdAt(tpUserStub.getCreatedAt().toString())
                .updatedAt(tpUserStub.getUpdatedAt().toString())
                .build();
        var accessToken = jwtService.issueToken(principal);

        Comment commentStub = Comment.builder()
                .body("first test content")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        commentsRepository.save(commentStub);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.delete("/comments/{id}", -2).header("Authorization", "Bearer " + accessToken));
        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
