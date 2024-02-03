package org.trickyplay.trickyplayapi.replies.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.*;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class RepliesServiceUnitTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private TPUserRepository tpUserRepository;
    @InjectMocks
    private RepliesService repliesService;

//    @BeforeEach
//    void setUp() {
//        commentRepository = Mockito.mock(CommentRepository.class);
//        replyRepository = Mockito.mock((ReplyRepository.class);
//        tpUserRepository = Mockito.mock((TPUserRepresentation.class);
//        commentsService = new CommentsService(commentRepository, replyRepository, tpUserRepository);
//    }

    // getRepliesByParentCommentId tests -----------------------------------------------
    @Test
    void given_1User1Comment2RepliesMockedInRepository_when_getRepliesByParentCommentIdIsCalled_then_returnCorrespondingGetRepliesResponse() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .id(1L)
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply firstReply = Reply.builder()
                .id(1L)
                .body("first reply body")
                .author(tPUserStub)
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .build();

        Reply secondReply = Reply.builder()
                .id(2L)
                .body("second reply body")
                .author(tPUserStub)
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .build();

        List<Reply> replies = List.of(firstReply, secondReply);
        Pageable page0Size10 = PageRequest.of(0, 10);
        Page<Reply> repliesPageStub = new PageImpl<>(replies, page0Size10, replies.size());
        Mockito.when(replyRepository.findAllByParentCommentId(commentStub.getId(), PageRequest.of(0, 10, Sort.Direction.ASC, "id")))
                .thenReturn(repliesPageStub);

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
    }

    // getRepliesByAuthorId tests -----------------------------------------------
    @Test
    void given_1User1Comment2RepliesMockedInRepository_when_getRepliesByAuthorIdIsCalled_then_returnCorrespondingGetRepliesResponse() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .id(1L)
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply firstReply = Reply.builder()
                .id(1L)
                .body("first reply body")
                .author(tPUserStub)
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .build();

        Reply secondReply = Reply.builder()
                .id(2L)
                .body("second reply body")
                .author(tPUserStub)
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .build();

        List<Reply> replies = List.of(firstReply, secondReply);
        Pageable page0Size10 = PageRequest.of(0, 10);
        Page<Reply> repliesPageStub = new PageImpl<>(replies, page0Size10, replies.size());
        Mockito.when(replyRepository.findAllByAuthorId(tPUserStub.getId(), PageRequest.of(0, 10, Sort.Direction.ASC, "id")))
                .thenReturn(repliesPageStub);

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
    }

    // getSingleReply tests -----------------------------------------------
    @Test
    void given_1User1Comment1ReplyMockedInRepository_when_getSingleReplyIsCalledWithSavedReplyId_then_returnCorrespondingReplyRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .id(1L)
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply replyStub = Reply.builder()
                .id(1L)
                .body("reply body")
                .author(tPUserStub)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .parentComment(commentStub)
                .build();

        Mockito.when(replyRepository.findById(replyStub.getId())).thenReturn(Optional.ofNullable(replyStub));

        // when
        ReplyRepresentation replyRepresentation = repliesService.getSingleReply(replyStub.getId());

        // then
        assertThat(replyRepresentation.getId()).isEqualTo(replyStub.getId());
        assertThat(replyRepresentation.getAuthor().getId()).isEqualTo(replyStub.getAuthor().getId());
    }

    @Test
    void given_mockedRepositoryThatReturnsEmptyOptional_when_getSingleReplyIsCalled_then_throwReplyNotFoundException() {
        // given
        Mockito.when(replyRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        // when,then
        assertThrows(ReplyNotFoundException.class, () -> repliesService.getSingleReply(1000L));
    }

    // addReply tests -----------------------------------------------
    @Test
    void given_1User1CommentMockedInRepositories_when_addReplyIsCalled_then_returnCorrespondingReplyRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .id(1L)
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Mockito.when(tpUserRepository.getReferenceById(tPUserStub.getId())).thenReturn(tPUserStub);
        Mockito.when(commentRepository.getReferenceById(commentStub.getId())).thenReturn(commentStub);
        Mockito.when(replyRepository.save(Mockito.any(Reply.class)))
                .thenAnswer(i -> {
                    // get the first argument passed
                    // in this case we only have one parameter, and it is on place 0
                    Reply replyArgument = i.getArgument(0, Reply.class);
                    return Reply.builder()
                            .author(replyArgument.getAuthor())
                            .id(1L)
                            .parentComment(replyArgument.getParentComment())
                            .body(replyArgument.getBody())
                            .createdAt(replyArgument.getCreatedAt())
                            .updatedAt(replyArgument.getUpdatedAt())
                            .build();
                });

        // when
        ReplyRepresentation replyRepresentation = repliesService.addReply(
                TPUserPrincipal.builder()
                        .id(tPUserStub.getId())
                        .name(tPUserStub.getName())
                        .role(tPUserStub.getRole().name())
                        .updatedAt(tPUserStub.getUpdatedAt().toString())
                        .createdAt(tPUserStub.getCreatedAt().toString())
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
    void given_1User1Comment1ReplyMockedInRepository_when_editReplyIsCalledWithSavedReplyIdAndItsAuthor_then_returnCorrespondingReplyRepresentation() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .id(1L)
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply replyStub = Reply.builder()
                .id(1L)
                .body("reply body")
                .author(tPUserStub)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .parentComment(commentStub)
                .build();

        Mockito.when(replyRepository.findById(replyStub.getId())).thenReturn(Optional.ofNullable(replyStub));

        // when
        ReplyRepresentation replyRepresentation = repliesService.editReply(
                replyStub.getId(),
                TPUserPrincipal.builder()
                        .name(replyStub.getAuthor().getName())
                        .id(replyStub.getAuthor().getId())
                        .role(Role.USER.name())
                        .createdAt(replyStub.getAuthor().getCreatedAt().toString())
                        .updatedAt(replyStub.getAuthor().getUpdatedAt().toString())
                        .build(),
                EditReplyRequest.builder()
                        .newReplyBody("new reply body")
                        .build()
        );

        // then
        assertThat(replyRepresentation.getBody()).isEqualTo("new reply body");
    }

    @Test
    void given_1User1Comment1ReplyMockedInRepository_when_editReplyIsCalledWithSavedReplyIdAndUserWhoIsNotItsAuthor_then_throwOperationNotAllowedException() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .id(1L)
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply replyStub = Reply.builder()
                .id(1L)
                .body("reply body")
                .author(tPUserStub)
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .parentComment(commentStub)
                .build();

        Mockito.when(replyRepository.findById(replyStub.getId())).thenReturn(Optional.ofNullable(replyStub));

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
    void given_mockedRepositoryThatReturnsEmptyOptional_when_editReplyIsCalledWithSavedReplyIdAndItsAuthor_then_throwReplyNotFoundException() {
        // given
        Mockito.when(replyRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        // when, then
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
    void given_1User1Comment1ReplyMockedInRepository_when_deleteReplyIsCalledWithSavedReplyIdAndItsAuthor_then_returnDeleteReplyResponse() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .id(1L)
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply replyStub = Reply.builder()
                .id(1L)
                .author(tPUserStub)
                .body("reply body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .parentComment(commentStub)
                .build();

        Mockito.when(replyRepository.findById(replyStub.getId())).thenReturn(Optional.ofNullable(replyStub));
        Mockito.doNothing().when(replyRepository).deleteById(Mockito.any());

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
    void given_1User1Comment1ReplyMockedInRepositories_when_deleteReplyIsCalledWithSavedReplyIdAndUserWhoIsNotItsAuthor_then_throwOperationNotAllowedException() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .id(1L)
                .author(tPUserStub)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply replyStub = Reply.builder()
                .id(1L)
                .author(tPUserStub)
                .body("reply body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .parentComment(commentStub)
                .build();

        Mockito.when(replyRepository.findById(replyStub.getId())).thenReturn(Optional.ofNullable(replyStub));

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
                        commentStub.getId()
                )
        );
    }

    @Test
    void given_mockedRepositoryThatReturnsEmptyOptional_when_deleteReplyIsCalledWithSavedReplyIdAndItsAuthor_then_throwReplyNotFoundException() {
        Mockito.when(replyRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        // when, then
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