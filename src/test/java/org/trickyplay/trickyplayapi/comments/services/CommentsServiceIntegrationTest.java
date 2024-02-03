package org.trickyplay.trickyplayapi.comments.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.comments.dtos.*;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.comments.records.CommentsPageArgs;
import org.trickyplay.trickyplayapi.comments.repositories.CommentRepository;
import org.trickyplay.trickyplayapi.general.exceptions.CommentNotFoundException;
import org.trickyplay.trickyplayapi.general.exceptions.OperationNotAllowedException;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.replies.repositories.ReplyRepository;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

//    use the following naming convention: Given_Preconditions_When_StateUnderTest_Then_ExpectedBehavior — Behavior-Driven Development (BDD)

@SpringBootTest
class CommentsServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ReplyRepository replyRepository;
    @Autowired
    private TPUserRepository tpUserRepository;
    @Autowired
    private CommentsService commentsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    // getCommentsByGameName tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User3CommentsSavedInDB_when_getCommentsByGameNameIsCalled_then_returnCorrespondingGetCommentsResponse() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("firstTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment firstCommentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Comment secondCommentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("second comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Comment thirdCommentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("third comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        TPUser savedTPUser = tpUserRepository.save(tPUserStub);
        Comment savedFirstCommentStub = commentRepository.save(firstCommentStub);
        Comment savedSecondCommentStub =commentRepository.save(secondCommentStub);
        Comment savedThirdCommentStub =commentRepository.save(thirdCommentStub);

        // when
        CommentsPageArgs commentsPageArgs = new CommentsPageArgs(0, 10, "id", Sort.Direction.ASC);
        GetCommentsResponse getCommentsResponse = commentsService.getCommentsByGameName("Snake", commentsPageArgs);

        // then
        assertThat(getCommentsResponse.getComments()).hasSize(3);
        assertThat(getCommentsResponse.getTotalElements()).isEqualTo(3);
        assertThat(getCommentsResponse.getPageSize()).isEqualTo(10);
        assertThat(getCommentsResponse.getComments().get(0).getAuthor().getId()).isEqualTo(savedTPUser.getId());
        assertThat(getCommentsResponse.getComments().get(0).getId()).isEqualTo(savedFirstCommentStub.getId());
        assertThat(getCommentsResponse.getComments().get(1).getId()).isEqualTo(savedSecondCommentStub.getId());
        assertThat(getCommentsResponse.getComments().get(2).getId()).isEqualTo(savedThirdCommentStub.getId());
    }

    // getCommentsByAuthorId tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User3CommentsSavedInDB_when_getCommentsByAuthorIdIsCalled_then_returnCorrespondingGetCommentsResponse() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("firstTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment firstCommentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Comment secondCommentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("second comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Comment thirdCommentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("third comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        TPUser savedTPUser = tpUserRepository.save(tPUserStub);
        Comment savedFirstCommentStub = commentRepository.save(firstCommentStub);
        Comment savedSecondCommentStub =commentRepository.save(secondCommentStub);
        Comment savedThirdCommentStub =commentRepository.save(thirdCommentStub);

        // when
        CommentsPageArgs commentsPageArgs = new CommentsPageArgs(0, 10, "id", Sort.Direction.ASC);
        GetCommentsResponse getCommentsResponse = commentsService.getCommentsByAuthorId(tPUserStub.getId(), commentsPageArgs);

        // then
        assertThat(getCommentsResponse.getComments()).hasSize(3);
        assertThat(getCommentsResponse.getComments().get(0).getAuthor().getId()).isEqualTo(savedTPUser.getId());
        assertThat(getCommentsResponse.getTotalElements()).isEqualTo(3);
        assertThat(getCommentsResponse.getPageSize()).isEqualTo(10);
        assertThat(getCommentsResponse.getComments().get(0).getAuthor().getId()).isEqualTo(savedTPUser.getId());
        assertThat(getCommentsResponse.getComments().get(0).getId()).isEqualTo(savedFirstCommentStub.getId());
        assertThat(getCommentsResponse.getComments().get(1).getId()).isEqualTo(savedSecondCommentStub.getId());
        assertThat(getCommentsResponse.getComments().get(2).getId()).isEqualTo(savedThirdCommentStub.getId());
    }

    // getSingleComment tests -----------------------------------------------
    @Test
    @Transactional
    void given_1Comment1UserSavedInDB_when_getSingleCommentIsCalledWithIdThatExistsInDB_then_returnCorrespondingCommentRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("firstTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        TPUser savedUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);

        // when
        CommentRepresentation commentRepresentation = commentsService.getSingleComment(savedCommentStub.getId());

        // then
        assertThat(commentRepresentation.getAuthor().getId()).isEqualTo(savedUserStub.getId());
        assertThat(commentRepresentation.getId()).isEqualTo(savedCommentStub.getId());
    }

    @Test
    @Transactional
    void given_1Comment1UserSavedInDB_when_getSingleCommentIsCalledWithIdNotExistingInDB_then_throwCommentNotFoundException() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("firstTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        TPUser savedUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);

        CommentNotFoundException thrown = assertThrows( // then
                CommentNotFoundException.class,
                () -> commentsService.getSingleComment(12345L), // when
                "Expected getSingleComment() to throw, but it didn't"
        );
    }

    // getCommentsWithReplies tests ------------------------------------------
    @Test
    @Transactional
    void given_1Comment2RepliesSavedInDB_when_getCommentsWithRepliesIsCalled_then_returnListOfCommentsWithTheirRepliesAttached() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("firstTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();
        Reply firstReplyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("first reply body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(2))
                .build();
        Reply secondReplyStub = Reply.builder()
                .parentComment(commentStub)
                .author(tPUserStub)
                .body("first reply body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(2))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(1))
                .build();
        TPUser savedUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);
        Reply savedFirstReplyStub = replyRepository.save(firstReplyStub);
        Reply savedSecondReplyStub = replyRepository.save(secondReplyStub);

        // when
        List<Comment> comments = commentsService.getCommentsWithReplies(0, 10, "id", Sort.Direction.ASC);

        // then
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getReplies()).hasSize(2);
    }

    // addComment tests ----------------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_addCommentIsCalled_then_returnCorrespondingCommentRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("firstTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser savedUserStub = tpUserRepository.save(tPUserStub);

        // when
        CommentRepresentation commentRepresentation = commentsService.addComment(
                TPUserPrincipal.builder()
                        .id(savedUserStub.getId())
                        .name(savedUserStub.getName())
                        .role(savedUserStub.getRole().name())
                        .updatedAt(savedUserStub.getUpdatedAt().toString())
                        .createdAt(savedUserStub.getCreatedAt().toString())
                        .build(),
                AddCommentRequest.builder()
                        .gameName("Snake")
                        .body("test comment body")
                        .build()
        );

        // then
        assertThat(commentRepresentation.getAuthor().getId()).isEqualTo(savedUserStub.getId());
        assertThat(commentRepresentation.getBody()).isEqualTo("test comment body");
        assertThat(commentRepresentation.getGameName()).isEqualTo("Snake");
    }

    // editComment tests ----------------------------------------------------
    @Test
    @Transactional
    void given_1Comment1UserSavedInDB_when_editCommentIsCalledWithSavedCommentIdAndItsAuthor_then_returnCorrespondingCommentRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("firstTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        TPUser savedUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);

        // when
        CommentRepresentation commentRepresentation = commentsService.editComment(
                TPUserPrincipal.builder()
                        .id(savedUserStub.getId())
                        .name(savedUserStub.getName())
                        .role(savedUserStub.getRole().name())
                        .updatedAt(savedUserStub.getUpdatedAt().toString())
                        .createdAt(savedUserStub.getCreatedAt().toString())
                        .build(),
                savedCommentStub.getId(),
                EditCommentRequest.builder()
                        .newCommentBody("new comment body")
                        .build()
        );

        // then
        assertThat(commentRepresentation.getAuthor().getId()).isEqualTo(savedUserStub.getId());
        assertThat(commentRepresentation.getBody()).isEqualTo("new comment body");
    }

    @Test
    @Transactional
    void given_1Comment2UsersSavedInDB_when_editCommentIsCalledWithSavedCommentIdAndUserWhoIsNotItsAuthor_then_throwOperationNotAllowedException() {
        // given
        TPUser firstTPUserStub = TPUser.builder()
                .name("firstTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser secondTPUserStub = TPUser.builder()
                .name("secondTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(firstTPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        TPUser savedFirstUserStub = tpUserRepository.save(firstTPUserStub);
        TPUser savedSecondUserStub = tpUserRepository.save(secondTPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);

        OperationNotAllowedException thrown = assertThrows( // then
                OperationNotAllowedException.class,
                () -> commentsService.editComment(
                        TPUserPrincipal.builder()
                                .id(savedSecondUserStub.getId())
                                .name(savedSecondUserStub.getName())
                                .role(savedSecondUserStub.getRole().name())
                                .updatedAt(savedSecondUserStub.getUpdatedAt().toString())
                                .createdAt(savedSecondUserStub.getCreatedAt().toString())
                                .build(),
                        savedCommentStub.getId(),
                        EditCommentRequest.builder()
                                .newCommentBody("new comment body")
                                .build()
                ), // when
                "Expected editComment() to throw, but it didn't"
        );
    }

    @Test
    @Transactional
    void given_1Comment1UserSavedInDB_when_editCommentIsCalledWithCommentIdNotFoundInDB_then_throwCommentNotFoundException() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("firstTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        TPUser savedUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);

        CommentNotFoundException thrown = assertThrows( // then
                CommentNotFoundException.class,
                () -> commentsService.editComment(
                        TPUserPrincipal.builder()
                                .id(savedUserStub.getId())
                                .name(savedUserStub.getName())
                                .role(savedUserStub.getRole().name())
                                .updatedAt(savedUserStub.getUpdatedAt().toString())
                                .createdAt(savedUserStub.getCreatedAt().toString())
                                .build(),
                        123456L,
                        EditCommentRequest.builder()
                                .newCommentBody("new comment body")
                                .build()
                ), // when
                "Expected editComment() to throw, but it didn't"
        );
    }

    // deleteComment tests --------------------------------------------------
    @Test
    @Transactional
    void given_1Comment1UserSavedInDB_when_deleteCommentIsCalledWithSavedCommentIdAndItsAuthor_then_returnDeleteCommentResponse() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("firstTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        TPUser savedUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);

        // when
        DeleteCommentResponse deleteCommentResponse = commentsService.deleteComment(
                TPUserPrincipal.builder()
                        .id(savedUserStub.getId())
                        .name(savedUserStub.getName())
                        .role(savedUserStub.getRole().name())
                        .updatedAt(savedUserStub.getUpdatedAt().toString())
                        .createdAt(savedUserStub.getCreatedAt().toString())
                        .build(),
                savedCommentStub.getId()
        );

        // then
        assertThat(deleteCommentResponse.getMessage()).isEqualTo("Comment successfully removed");
    }

    @Test
    @Transactional
    void given_1Comment2UsersSavedInDB_when_deleteCommentIsCalledWithSavedCommentIdAndUserWhoIsNotItsAuthor_then_operationNotAllowedExceptionsIsThrew() {
        // given
        TPUser firstTPUserStub = TPUser.builder()
                .name("firstTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser secondTPUserStub = TPUser.builder()
                .name("secondTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(firstTPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        TPUser savedFirstUserStub = tpUserRepository.save(firstTPUserStub);
        TPUser savedSecondUserStub = tpUserRepository.save(secondTPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);

        OperationNotAllowedException thrown = assertThrows( // then
                OperationNotAllowedException.class,
                () -> commentsService.deleteComment(
                        TPUserPrincipal.builder()
                                .id(savedSecondUserStub.getId())
                                .name(savedSecondUserStub.getName())
                                .role(savedSecondUserStub.getRole().name())
                                .updatedAt(savedSecondUserStub.getUpdatedAt().toString())
                                .createdAt(savedSecondUserStub.getCreatedAt().toString())
                                .build(),
                        savedCommentStub.getId()
                ),
                "Expected editComment() to throw, but it didn't"
        );
    }

    @Test
    @Transactional
    void given_1Comment1UserSavedInDB_when_deleteCommentIsCalledWithCommentIdNotPresentInDB_then_throwCommentNotFoundExceptions() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("firstTestUser")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        TPUser savedUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);

        CommentNotFoundException thrown = assertThrows( // then
                CommentNotFoundException.class,
                () -> commentsService.deleteComment(
                        TPUserPrincipal.builder()
                                .id(savedUserStub.getId())
                                .name(savedUserStub.getName())
                                .role(savedUserStub.getRole().name())
                                .updatedAt(savedUserStub.getUpdatedAt().toString())
                                .createdAt(savedUserStub.getCreatedAt().toString())
                                .build(),
                        123456L
                ),
                "Expected editComment() to throw, but it didn't"
        );
    }
}