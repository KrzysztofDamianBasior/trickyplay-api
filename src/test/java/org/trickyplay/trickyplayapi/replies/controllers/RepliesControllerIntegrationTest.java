package org.trickyplay.trickyplayapi.replies.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

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
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.comments.repositories.CommentRepository;
import org.trickyplay.trickyplayapi.replies.dtos.AddReplyRequest;
import org.trickyplay.trickyplayapi.replies.dtos.EditReplyRequest;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
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
class RepliesControllerIntegrationTest extends BaseIntegrationTest {

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

    // getRepliesByParentCommentId tests -----------------------------------------------

    @Test
    @Transactional
    void given_1User1Comment3RepliesSavedInDB_when_repliesFeedGetEndpointIsHitWithValidParams_then_returnCorrespondingGetRepliesResponse() throws Exception {
        TPUser tPUserStub = TPUser.builder()
                .name("testUser")
                .password(passwordEncoder.encode("123ASDasd"))
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        Comment commentStub = Comment.builder()
                .body("comment body")
                .gameName("Snake")
                .author(tPUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Reply firstReplyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("first reply body")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Reply secondReplyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("second reply body")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Reply thirdReplyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("third reply body")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Long savedTPUserStubId = tpUserRepository.save(tPUserStub).getId();
        Long savedCommentStubId = commentsRepository.save(commentStub).getId();
        Long savedFirstReplyStubId = replyRepository.save(firstReplyStub).getId();
        Long savedSecondReplyStubId = replyRepository.save(secondReplyStub).getId();
        Long savedThirdReplyStubId = replyRepository.save(thirdReplyStub).getId();

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/replies/feed")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("parentCommentId", savedCommentStubId.toString())
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
    void given_1User1Comment3RepliesSavedInDB_when_repliesFeedGetEndpointIsHitWithInvalidParams_then_returnBadRequestResponse() throws Exception {
        TPUser tPUserStub = TPUser.builder()
                .name("testUser")
                .password(passwordEncoder.encode("123ASDasd"))
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        Comment commentStub = Comment.builder()
                .body("comment body")
                .gameName("Snake")
                .author(tPUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Reply firstReplyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("first reply body")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Reply secondReplyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("second reply body")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Reply thirdReplyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("third reply body")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Long savedTPUserStubId = tpUserRepository.save(tPUserStub).getId();
        Long savedCommentStubId = commentsRepository.save(commentStub).getId();
        Long savedFirstReplyStubId = replyRepository.save(firstReplyStub).getId();
        Long savedSecondReplyStubId = replyRepository.save(secondReplyStub).getId();
        Long savedThirdReplyStubId = replyRepository.save(thirdReplyStub).getId();

        mockMvc.perform(MockMvcRequestBuilders.get("/replies/feed")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .param("parentCommentId", "-10")
                .param("pageNumber", "0")
                .param("pageSize", "10")
                .param("sortBy", "id")
                .param("orderDirection", "Asc")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.get("/replies/feed")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .param("parentCommentId", savedCommentStubId.toString())
                .param("pageNumber", "-10")
                .param("pageSize", "-10")
                .param("sortBy", "badSortBy")
                .param("orderDirection", "badDirection")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.get("/replies/feed")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .param("parentCommentId", savedCommentStubId.toString())
                .param("pageNumber", "0")
                .param("pageSize", "-10")
                .param("sortBy", "id")
                .param("orderDirection", "Asc")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.get("/replies/feed")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .param("parentCommentId", savedCommentStubId.toString())
                .param("pageNumber", "0")
                .param("pageSize", "10")
                .param("sortBy", "id")
                .param("sortBy", "badSortBy")
                .param("orderDirection", "Asc")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.get("/replies/feed")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .param("parentCommentId", savedCommentStubId.toString())
                .param("pageNumber", "0")
                .param("pageSize", "10")
                .param("sortBy", "id")
                .param("orderDirection", "badDirection")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // getSingleReply tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1Comment1ReplySavedInDB_when_repliesGetEndpointIsHitWithValidPathVar_then_returnCorrespondingReplyRepresentation() throws Exception {
        TPUser tPUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();

        Comment commentStub = Comment.builder()
                .body("comment body")
                .gameName("Snake")
                .author(tPUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Reply replyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("reply body")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentsRepository.save(commentStub);
        Reply savedReplyStub = replyRepository.save(replyStub);

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/replies/{id}", savedReplyStub.getId().toString())
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedReplyStub.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.parentComment.id").value(savedReplyStub.getParentComment().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").value(savedReplyStub.getBody()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.author.id").value(savedReplyStub.getAuthor().getId()))
                .andDo(requestResult -> {
                    String json = requestResult.getResponse().getContentAsString();
                    LocalDateTime createdAt = LocalDateTime.parse(JsonPath.parse(json).read("$.updatedAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime updatedAt = LocalDateTime.parse(JsonPath.parse(json).read("$.createdAt").toString(), DateTimeFormatter.ISO_DATE_TIME);
                    assertThat(createdAt).isEqualTo(savedReplyStub.getCreatedAt());
                    assertThat(updatedAt).isEqualTo(savedReplyStub.getUpdatedAt());
                })
                .andReturn();
    }

    @Test
    @Transactional
    void given_1User1Comment1ReplySavedInDB_when_repliesGetEndpointIsHitWithAbsentIdAsPathVar_then_returnNotFoundResponse() throws Exception {
        TPUser tPUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();

        Comment commentStub = Comment.builder()
                .body("comment body")
                .gameName("Snake")
                .author(tPUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Reply replyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("reply body")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Long tPUserStubId = tpUserRepository.save(tPUserStub).getId();
        Long commentStubId = commentsRepository.save(commentStub).getId();
        Long replyStubId = replyRepository.save(replyStub).getId();

        Long idOfCommentNotPresentInTheDatabase = 123456789L;

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/replies/{id}", idOfCommentNotPresentInTheDatabase)
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    @Test
    @Transactional
    void given_1User1Comment1ReplySavedInDB_when_repliesGetEndpointIsHitWithInvalidPathVar_then_returnBadRequestResponse() throws Exception {
        TPUser tPUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();

        Comment commentStub = Comment.builder()
                .body("comment body")
                .gameName("Snake")
                .author(tPUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Reply replyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("reply body")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Long tPUserStubId = tpUserRepository.save(tPUserStub).getId();
        Long commentStubId = commentsRepository.save(commentStub).getId();
        Long replyStubId = replyRepository.save(replyStub).getId();

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/replies/{id}", "-2")
                )
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    // addReply tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1CommentSavedInDB_when_repliesPostEndpointIsHitWithValidRequestBody_then_returnCorrespondingReplyRepresentation() throws Exception {
        TPUser tPUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);

        Comment commentStub = Comment.builder()
                .body("comment body")
                .gameName("Snake")
                .author(tPUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Comment savedCommentStub = commentsRepository.save(commentStub);

        TPUserPrincipal principal = TPUserPrincipal.builder()
                .id(savedTPUserStub.getId())
                .name(savedTPUserStub.getName())
                .password(null)
                .role(Role.USER.name())
                .createdAt(savedTPUserStub.getCreatedAt().toString())
                .updatedAt(savedTPUserStub.getUpdatedAt().toString())
                .build();
        var accessToken = jwtService.issueToken(principal);

        AddReplyRequest addReplyRequest = AddReplyRequest.builder()
                .body("add reply body")
                .parentCommentId(savedCommentStub.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/replies")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReplyRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.updatedAt").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.parentComment.id").value(addReplyRequest.getParentCommentId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").value(addReplyRequest.getBody()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.author.id").value(principal.getId()));
    }

    @Test
    @Transactional
    void given_1User1CommentSavedInDB_when_repliesPostEndpointIsHitWithInvalidRequestBody_then_returnBadRequestResponse() throws Exception {
        TPUser tPUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        TPUser savedTPUserStub = tpUserRepository.save(tPUserStub);

        Comment commentStub = Comment.builder()
                .body("comment body")
                .gameName("Snake")
                .author(tPUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Comment savedCommentStub = commentsRepository.save(commentStub);

        TPUserPrincipal principal = TPUserPrincipal.builder()
                .id(savedTPUserStub.getId())
                .name(savedTPUserStub.getName())
                .password(null)
                .role(Role.USER.name())
                .createdAt(savedTPUserStub.getCreatedAt().toString())
                .updatedAt(savedTPUserStub.getUpdatedAt().toString())
                .build();
        var accessToken = jwtService.issueToken(principal);

        AddReplyRequest addReplyRequest = AddReplyRequest.builder()
                .body("")
                .parentCommentId(-1L)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/replies")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReplyRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // editReply tests -----------------------------------------------
    @Test
    @Transactional
    void given_1Comment1User1ReplySavedInDB_when_repliesPatchEndpointIsHitWithValidBothRequestBodyAndPathVar_then_returnCorrespondingReplyRepresentation() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        TPUser savedTPUser = tpUserRepository.save(tpUserStub);

        TPUserPrincipal principal = TPUserPrincipal.builder()
                .id(savedTPUser.getId())
                .name(tpUserStub.getName())
                .password(null)
                .role(Role.USER.name())
                .createdAt(tpUserStub.getCreatedAt().toString())
                .updatedAt(tpUserStub.getUpdatedAt().toString())
                .build();
        var accessToken = jwtService.issueToken(principal);

        Comment commentStub = Comment.builder()
                .body("comment body")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Comment savedCommentStub = commentsRepository.save(commentStub);

        Reply replyStub = Reply.builder()
                .author(savedTPUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .parentComment(savedCommentStub)
                .body("old reply body")
                .build();
        Reply savedReplyStub = replyRepository.save(replyStub);

        EditReplyRequest editReplyRequest = EditReplyRequest.builder()
                .newReplyBody("new reply body")
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.patch("/replies/{id}", savedReplyStub.getId())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(editReplyRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        response.andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").value(editReplyRequest.getNewReplyBody()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.parentComment.id").value(savedCommentStub.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.updatedAt").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.author.id").value(savedReplyStub.getAuthor().getId()));
    }

    @Test
    @Transactional
    void given_1Comment1User1ReplySavedInDB_when_repliesPatchEndpointIsHitWithInvalidRequestBodyOrPathVar_then_returnBadRequestResponse() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        TPUser savedTPUser = tpUserRepository.save(tpUserStub);

        TPUserPrincipal principal = TPUserPrincipal.builder()
                .id(savedTPUser.getId())
                .name(tpUserStub.getName())
                .password(null)
                .role(Role.USER.name())
                .createdAt(tpUserStub.getCreatedAt().toString())
                .updatedAt(tpUserStub.getUpdatedAt().toString())
                .build();
        var accessToken = jwtService.issueToken(principal);

        Comment commentStub = Comment.builder()
                .body("comment body")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Comment savedCommentStub = commentsRepository.save(commentStub);

        Reply replyStub = Reply.builder()
                .author(savedTPUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .parentComment(savedCommentStub)
                .body("old reply body")
                .build();
        Reply savedReplyStub = replyRepository.save(replyStub);

        mockMvc.perform(MockMvcRequestBuilders.patch("/replies/{id}", "0")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(EditReplyRequest.builder().newReplyBody("").build())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.patch("/replies/{id}", "-1")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(EditReplyRequest.builder().newReplyBody("test").build())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.patch("/replies/{id}", "0")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(EditReplyRequest.builder().newReplyBody(
                                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" +
                                        "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" +
                                        "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" +
                                        "1"
                        ).build())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // deleteReply tests -----------------------------------------------
    @Test
    @Transactional
    void given_1Comment1User1ReplySavedInDB_when_repliesDeleteEndpointIsHitWithValidPathVar_then_returnCorrespondingDeleteReplyResponse() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        TPUser savedTPUser = tpUserRepository.save(tpUserStub);

        TPUserPrincipal principal = TPUserPrincipal.builder()
                .id(savedTPUser.getId())
                .name(tpUserStub.getName())
                .password(null)
                .role(Role.USER.name())
                .createdAt(tpUserStub.getCreatedAt().toString())
                .updatedAt(tpUserStub.getUpdatedAt().toString())
                .build();
        var accessToken = jwtService.issueToken(principal);

        Comment commentStub = Comment.builder()
                .body("comment body")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Comment savedCommentStub = commentsRepository.save(commentStub);

        Reply replyStub = Reply.builder()
                .author(savedTPUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .parentComment(savedCommentStub)
                .body("reply body")
                .build();
        Reply savedReplyStub = replyRepository.save(replyStub);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.delete("/replies/{id}", savedReplyStub.getId())
                .header("Authorization", "Bearer " + accessToken)
        );

        response.andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Reply successfully removed"));
    }

    @Test
    @Transactional
    void given_1Comment1User1ReplySavedInDB_when_repliesDeleteIsHitWithInvalidPathVar_then_returnBadRequestResponse() throws Exception {
        TPUser tpUserStub = TPUser.builder()
                .name("testUser")
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode("123ASDasd"))
                .build();
        TPUser savedTPUser = tpUserRepository.save(tpUserStub);

        TPUserPrincipal principal = TPUserPrincipal.builder()
                .id(savedTPUser.getId())
                .name(tpUserStub.getName())
                .password(null)
                .role(Role.USER.name())
                .createdAt(tpUserStub.getCreatedAt().toString())
                .updatedAt(tpUserStub.getUpdatedAt().toString())
                .build();
        var accessToken = jwtService.issueToken(principal);

        Comment commentStub = Comment.builder()
                .body("comment body")
                .gameName("Snake")
                .author(tpUserStub)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Comment savedCommentStub = commentsRepository.save(commentStub);

        Reply replyStub = Reply.builder()
                .author(savedTPUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .parentComment(savedCommentStub)
                .body("reply body")
                .build();
        Reply savedReplyStub = replyRepository.save(replyStub);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.delete("/replies/{id}", -2)
                .header("Authorization", "Bearer " + accessToken)
        );

        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}