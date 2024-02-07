package org.trickyplay.trickyplayapi.replies.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.comments.repositories.CommentRepository;
import org.trickyplay.trickyplayapi.general.exceptions.OperationNotAllowedException;
import org.trickyplay.trickyplayapi.general.exceptions.ReplyNotFoundException;
import org.trickyplay.trickyplayapi.replies.dtos.*;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.replies.records.RepliesPageArgs;
import org.trickyplay.trickyplayapi.replies.repositories.ReplyRepository;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

//    use the following naming convention: Given_Preconditions_When_StateUnderTest_Then_ExpectedBehavior — Behavior-Driven Development (BDD)

@SpringBootTest
class RepliesServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ReplyRepository replyRepository;
    @Autowired
    private TPUserRepository tpUserRepository;
    @Autowired
    private RepliesService repliesService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    // getRepliesByParentCommentId tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1Comment2RepliesSavedInDB_when_getRepliesByParentCommentIdIsCalled_then_returnCorrespondingGetRepliesResponse() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply firstReply = Reply.builder()
                .body("first reply body")
                .author(tPUserStub)
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .build();

        Reply secondReply = Reply.builder()
                .body("second reply body")
                .author(tPUserStub)
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .build();

        tpUserRepository.save(tPUserStub);
        commentRepository.save(commentStub);
        Reply savedFirstReply = replyRepository.save(firstReply);
        Reply savedSecondReply = replyRepository.save(secondReply);

        // when
        RepliesPageArgs repliesPageArgs = new RepliesPageArgs(0, 10, "id", Sort.Direction.ASC);
        GetRepliesResponse getRepliesResponse = repliesService.getRepliesByParentCommentId(commentStub.getId(), repliesPageArgs);

        // then
        assertThat(getRepliesResponse.getReplies())
                .hasSize(2)
                .doesNotHaveDuplicates()
                .doesNotContainNull();
        assertThat(getRepliesResponse.isLast()).isTrue();
        assertThat(getRepliesResponse.getTotalElements()).isEqualTo(2);
        assertThat(getRepliesResponse.getTotalPages()).isEqualTo(1);
        assertThat(getRepliesResponse.getPageSize()).isEqualTo(10);
        assertThat(getRepliesResponse.getReplies().get(0).getAuthor().getId()).isEqualTo(tPUserStub.getId());
        assertThat(getRepliesResponse.getReplies().get(0).getId()).isEqualTo(savedFirstReply.getId());
        assertThat(getRepliesResponse.getReplies().get(1).getId()).isEqualTo(savedSecondReply.getId());
    }

    // getRepliesByAuthorId tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1Comment2RepliesSavedInDB_when_getRepliesByAuthorIdIsCalled_then_returnCorrespondingGetRepliesResponse() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply firstReply = Reply.builder()
                .body("first reply body")
                .author(tPUserStub)
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .build();

        Reply secondReply = Reply.builder()
                .body("second reply body")
                .author(tPUserStub)
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .build();

        tpUserRepository.save(tPUserStub);
        commentRepository.save(commentStub);
        Reply savedFirstReply = replyRepository.save(firstReply);
        Reply savedSecondReply = replyRepository.save(secondReply);

        // when
        RepliesPageArgs repliesPageArgs = new RepliesPageArgs(0, 10, "id", Sort.Direction.ASC);
        GetRepliesResponse getRepliesResponse = repliesService.getRepliesByAuthorId(tPUserStub.getId(), repliesPageArgs);

        // then
        assertThat(getRepliesResponse.getReplies())
                .hasSize(2)
                .doesNotHaveDuplicates()
                .doesNotContainNull();
        assertThat(getRepliesResponse.isLast()).isTrue();
        assertThat(getRepliesResponse.getTotalElements()).isEqualTo(2);
        assertThat(getRepliesResponse.getTotalPages()).isEqualTo(1);
        assertThat(getRepliesResponse.getPageSize()).isEqualTo(10);
        assertThat(getRepliesResponse.getReplies().get(0).getAuthor().getId()).isEqualTo(tPUserStub.getId());
        assertThat(getRepliesResponse.getReplies().get(0).getId()).isEqualTo(savedFirstReply.getId());
        assertThat(getRepliesResponse.getReplies().get(1).getId()).isEqualTo(savedSecondReply.getId());
    }

    // getSingleReply tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1Comment1ReplySavedInDB_when_getSingleReplyIsCalledWithSavedReplyId_then_returnCorrespondingReplyRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply replyStub = Reply.builder()
                .body("reply body")
                .author(tPUserStub)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .parentComment(commentStub)
                .build();

        TPUser savedUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);
        Reply savedReplyStub = replyRepository.save(replyStub);

        // when
        ReplyRepresentation replyRepresentation = repliesService.getSingleReply(replyStub.getId());

        // then
        assertThat(replyRepresentation.getId()).isEqualTo(savedReplyStub.getId());
        assertThat(replyRepresentation.getAuthor().getId()).isEqualTo(savedReplyStub.getAuthor().getId());
    }

    @Test
    @Transactional
    void given_emptyDB_when_getSingleReplyIsCalledWithIdNotPresentInDB_then_throwReplyNotFoundException() {
        assertThrows(ReplyNotFoundException.class, () -> repliesService.getSingleReply(1000L));
    }

    // addReply tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1CommentSavedInDB_when_addReplyIsCalled_then_returnCorrespondingReplyRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        TPUser savedUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);

        // when
        ReplyRepresentation replyRepresentation = repliesService.addReply(
                TPUserPrincipal.builder()
                        .id(savedUserStub.getId())
                        .name(savedUserStub.getName())
                        .role(savedUserStub.getRole().name())
                        .updatedAt(savedUserStub.getUpdatedAt().toString())
                        .createdAt(savedUserStub.getCreatedAt().toString())
                        .build(),
                AddReplyRequest.builder()
                        .body("new reply body")
                        .parentCommentId(commentStub.getId())
                        .build()
        );

        // then
        assertThat(replyRepresentation.getParentComment().getId()).isEqualTo(commentStub.getId());
        assertThat(replyRepresentation.getBody()).isEqualTo("new reply body");
    }

    // editReply tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1Comment1ReplySavedInDB_when_editReplyIsCalledWithSavedReplyIdAndItsAuthor_then_returnCorrespondingReplyRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply replyStub = Reply.builder()
                .body("reply body")
                .author(tPUserStub)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .parentComment(commentStub)
                .build();

        TPUser savedUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);
        Reply savedReplyStub = replyRepository.save(replyStub);

        // when
        ReplyRepresentation replyRepresentation = repliesService.editReply(
                replyStub.getId(),
                TPUserPrincipal.builder()
                        .name(savedReplyStub.getAuthor().getName())
                        .id(savedReplyStub.getAuthor().getId())
                        .role(Role.USER.name())
                        .createdAt(savedReplyStub.getAuthor().getCreatedAt().toString())
                        .updatedAt(savedReplyStub.getAuthor().getUpdatedAt().toString())
                        .build(),
                EditReplyRequest.builder()
                        .newReplyBody("new reply body")
                        .build()
        );

        // then
        assertThat(replyRepresentation.getBody()).isEqualTo("new reply body");
    }

    @Test
    @Transactional
    void given_1User1Comment1ReplySavedInDB_when_editReplyIsCalledWithSavedReplyIdAndUserWhoIsNotItsAuthor_then_throwOperationNotAllowedException() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply replyStub = Reply.builder()
                .body("reply body")
                .author(tPUserStub)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .parentComment(commentStub)
                .build();

        TPUser savedUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);
        Reply savedReplyStub = replyRepository.save(replyStub);

        OperationNotAllowedException thrown = assertThrows(OperationNotAllowedException.class, () -> repliesService.editReply(
                replyStub.getId(),
                TPUserPrincipal.builder()
                        .name("unauthorizedUser")
                        .id(123L)
                        .role(Role.USER.name())
                        .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10).toString())
                        .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10).toString())
                        .build(),
                EditReplyRequest.builder()
                        .newReplyBody("new reply body")
                        .build())
        );
    }

    @Test
    @Transactional
    void given_emptyDB_when_editReplyIsCalledWithReplyIdNotPresentInDB_then_throwReplyNotFoundException() {
        ReplyNotFoundException thrown = assertThrows(ReplyNotFoundException.class, () -> repliesService.editReply(
                12345L,
                TPUserPrincipal.builder()
                        .name("user")
                        .role(Role.USER.name())
                        .id(123L)
                        .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10).toString())
                        .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10).toString())
                        .build(),
                EditReplyRequest.builder()
                        .newReplyBody("new reply body")
                        .build())
        );
    }

    // deleteReply tests -----------------------------------------------
    @Test
    @Transactional
    void given_1User1Comment1ReplySavedInDB_when_deleteReplyIsCalledWithSavedReplyIdAndItsAuthor_then_returnDeleteReplyResponse() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply replyStub = Reply.builder()
                .author(tPUserStub)
                .body("reply body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .parentComment(commentStub)
                .build();

        TPUser savedUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);
        Reply savedReplyStub = replyRepository.save(replyStub);

        // when
        DeleteReplyResponse deleteReplyResponse = repliesService.deleteReply(
                TPUserPrincipal.builder()
                        .name(replyStub.getAuthor().getName())
                        .id(replyStub.getAuthor().getId())
                        .role(Role.USER.name())
                        .createdAt(replyStub.getAuthor().getCreatedAt().toString())
                        .updatedAt(replyStub.getAuthor().getUpdatedAt().toString())
                        .build(),
                replyStub.getId()
        );

        // then
        assertThat(deleteReplyResponse.getMessage()).isEqualTo("Reply successfully removed");
    }

    @Test
    @Transactional
    void given_1User1Comment1ReplySavedInDB_when_deleteReplyIsCalledWithSavedReplyIdAndUserWhoIsNotItsAuthor_then_throwOperationNotAllowedException() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply replyStub = Reply.builder()
                .author(tPUserStub)
                .body("reply body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .parentComment(commentStub)
                .build();

        TPUser savedUserStub = tpUserRepository.save(tPUserStub);
        Comment savedCommentStub = commentRepository.save(commentStub);
        Reply savedReplyStub = replyRepository.save(replyStub);

        // when, then
        OperationNotAllowedException thrown = assertThrows(OperationNotAllowedException.class, () ->
                repliesService.deleteReply(
                        TPUserPrincipal.builder()
                                .name("unauthorizedUser")
                                .id(12345L)
                                .role(Role.USER.name())
                                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5).toString())
                                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5).toString())
                                .build(),
                        replyStub.getId()
                )
        );
    }

    @Test
    @Transactional
    void given_emptyDB_when_deleteReplyIsCalledWithReplyIdNotPresentInDB_then_throwReplyNotFoundException() {
        ReplyNotFoundException thrown = assertThrows(ReplyNotFoundException.class, () ->
                repliesService.deleteReply(
                        TPUserPrincipal.builder()
                                .name("user")
                                .id(1L)
                                .role(Role.USER.name())
                                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5).toString())
                                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5).toString())
                                .build(),
                        12345L
                )
        );
    }
}