package org.trickyplay.trickyplayapi.comments.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.*;

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
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CommentsServiceUnitTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private TPUserRepository tpUserRepository;
    @InjectMocks
    private CommentsService commentsService;

//    @BeforeEach
//    void setUp() {
//        commentRepository = Mockito.mock(CommentRepository.class);
//        replyRepository = Mockito.mock((ReplyRepository.class);
//        tpUserRepository = Mockito.mock((TPUserRepresentation.class);
//        commentsService = new CommentsService(commentRepository, replyRepository, tpUserRepository);
//    }

    // getCommentsByGameName tests -----------------------------------------------
    @Test
    void given_2Users3CommentsMockedInRepository_when_getCommentsByGameNameIsCalled_then_returnCorrespondingGetCommentsResponse() {
        // given
        TPUser firstTPUserStub = TPUser.builder()
                .id(1L)
                .name("firstUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser secondTPUserStub = TPUser.builder()
                .id(2L)
                .name("secondUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment firstCommentStub = Comment.builder()
                .id(1L)
                .author(firstTPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Comment secondCommentStub = Comment.builder()
                .id(2L)
                .author(firstTPUserStub)
                .gameName("Snake")
                .body("second comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Comment thirdCommentStub = Comment.builder()
                .id(3L)
                .author(firstTPUserStub)
                .gameName("Snake")
                .body("third comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Comment fourthCommentStub = Comment.builder()
                .id(4L)
                .author(secondTPUserStub)
                .gameName("TicTacToe")
                .body("third comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        List<Comment> snakeComments = List.of(firstCommentStub, secondCommentStub, thirdCommentStub);
        Pageable page0Size10 = PageRequest.of(0, 10);
        Page<Comment> commentsPageStub = new PageImpl<>(snakeComments, page0Size10, snakeComments.size());

        Mockito.when(commentRepository.findAllByGameName("Snake", PageRequest.of(0, 10, Sort.Direction.ASC, "id")))
                .thenReturn(commentsPageStub);

        // when
        CommentsPageArgs commentsPageArgs = new CommentsPageArgs(0, 10, "id", Sort.Direction.ASC);
        GetCommentsResponse getCommentsResponse = commentsService.getCommentsByGameName("Snake", commentsPageArgs);

        // then
        assertThat(getCommentsResponse.getComments())
                .hasSize(3)
                .doesNotHaveDuplicates()
                .doesNotContainNull();
        assertThat(getCommentsResponse.isLast()).isTrue();
        assertThat(getCommentsResponse.getTotalElements()).isEqualTo(3);
        assertThat(getCommentsResponse.getTotalPages()).isEqualTo(1);
        assertThat(getCommentsResponse.getPageSize()).isEqualTo(10);
        assertThat(getCommentsResponse.getComments().get(0).getAuthor().getId()).isEqualTo(firstTPUserStub.getId());
    }

    // getCommentsByAuthorId tests -----------------------------------------------
    @Test
    void given_1User3CommentsMockedInRepository_when_getCommentsByAuthorIdIsCalled_then_returnCorrespondingGetCommentsResponse() {
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

        Comment firstCommentStub = Comment.builder()
                .id(1L)
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Comment secondCommentStub = Comment.builder()
                .id(2L)
                .author(tPUserStub)
                .gameName("Snake")
                .body("second comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Comment thirdCommentStub = Comment.builder()
                .id(3L)
                .author(tPUserStub)
                .gameName("Snake")
                .body("third comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();


        List<Comment> firstUserComments = List.of(firstCommentStub, secondCommentStub, thirdCommentStub);
        Pageable page0Size10 = PageRequest.of(0, 10);
        Page<Comment> commentsPageStub = new PageImpl<>(firstUserComments, page0Size10, firstUserComments.size());

        Mockito.when(commentRepository.findAllByAuthorId(tPUserStub.getId(), PageRequest.of(0, 10, Sort.Direction.ASC, "id"))) // Mockito.any(Pageable.class)
                .thenReturn(commentsPageStub);

        // when
        CommentsPageArgs commentsPageArgs = new CommentsPageArgs(0, 10, "id", Sort.Direction.ASC);
        GetCommentsResponse getCommentsResponse = commentsService.getCommentsByAuthorId(tPUserStub.getId(), commentsPageArgs);

        // then
        assertThat(getCommentsResponse.getComments())
                .hasSize(3)
                .doesNotHaveDuplicates()
                .doesNotContainNull();
        assertThat(getCommentsResponse.isLast()).isTrue();
        assertThat(getCommentsResponse.getTotalElements()).isEqualTo(3);
        assertThat(getCommentsResponse.getPageSize()).isEqualTo(10);
        assertThat(getCommentsResponse.getTotalPages()).isEqualTo(1);
        assertThat(getCommentsResponse.getComments().get(0).getAuthor().getId()).isEqualTo(tPUserStub.getId());
    }

    // getSingleComment tests -----------------------------------------------
    @Test
    void given_1Comment1UserMockedInRepository_when_getSingleCommentIsCalledWithSavedCommentsId_then_returnCorrespondingCommentRepresentation() {
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

        Mockito.when(commentRepository.findById(commentStub.getId())).thenReturn(Optional.ofNullable(commentStub));
//        Mockito.when(commentRepository.findById(AdditionalMatchers.not(Mockito.eq(firstCommentStub.getId()))))
//                .thenThrow(new CommentNotFoundException(1000L));

        // when
        CommentRepresentation commentRepresentation = commentsService.getSingleComment(commentStub.getId());

        // then
        assertThat(commentRepresentation.getId()).isEqualTo(commentStub.getId());
        assertThat(commentRepresentation.getAuthor().getId()).isEqualTo(commentStub.getAuthor().getId());
    }

    @Test
    void given_mockedRepositoryThatReturnsEmptyOptional_when_getSingleCommentIsCalled_then_throwCommentNotFoundException() {
        // given
        Mockito.when(commentRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        // when,then
        assertThrows(CommentNotFoundException.class, () -> commentsService.getSingleComment(1000L));
    }

    // getCommentsWithReplies tests ------------------------------------------
    @Test
    void given_1CommentWith1ReplyAnd1CommentWith2RepliesMockedInRepositories_when_getCommentsWithRepliesIsCalled_then_returnListOfAllCommentsWithTheirRepliesAttached() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("firstTestUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment firstCommentStub = Comment.builder()
                .id(1L)
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Comment secondCommentStub =Comment.builder()
                .id(2L)
                .author(tPUserStub)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply firstReplyStub = Reply.builder()
                .id(0L)
                .author(tPUserStub)
                .parentComment(Comment.builder()
                        .id(1L)
                        .author(tPUserStub)
                        .gameName("Snake")
                        .body("first comment body")
                        .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                        .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                        .build())
                .body("first reply body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply secondReplyStub = Reply.builder()
                .id(1L)
                .author(tPUserStub)
                .parentComment(Comment.builder()
                        .id(1L)
                        .author(tPUserStub)
                        .gameName("Snake")
                        .body("first comment body")
                        .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                        .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                        .build())
                .body("second reply body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply thirdReplyStub = Reply.builder()
                .id(2L)
                .author(tPUserStub)
                .parentComment(Comment.builder()
                        .id(2L)
                        .author(tPUserStub)
                        .gameName("Snake")
                        .body("first comment body")
                        .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                        .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                        .build())
                .body("first reply body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        List<Comment> snakeComments = List.of(firstCommentStub, secondCommentStub);
        Pageable page0Size10 = PageRequest.of(0, 10);
        Page<Comment> commentsPageStub = new PageImpl<>(snakeComments, page0Size10, snakeComments.size());
        Mockito.when(commentRepository.findAll(PageRequest.of(0, 10, Sort.Direction.ASC, "id")))
                .thenReturn(commentsPageStub);
        Mockito.when(replyRepository.findAllByParentCommentIdIn(List.of(firstCommentStub.getId(), secondCommentStub.getId()))).thenReturn((List.of(firstReplyStub, secondReplyStub, thirdReplyStub)));

        // when
        List<Comment> comments = commentsService.getCommentsWithReplies(0, 10, "id", Sort.Direction.ASC);

        // then
        assertThat(comments)
                .hasSize(2)
                .doesNotHaveDuplicates()
                .doesNotContainNull();
        assertThat(comments.stream().filter(comment -> comment.getId() == firstCommentStub.getId()).findAny().get().getReplies()).hasSize(2);
    }

    // addComment tests ----------------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_addCommentIsCalled_then_returnCorrespondingCommentRepresentation() {
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

        Mockito.when(tpUserRepository.getReferenceById(tPUserStub.getId())).thenReturn(tPUserStub);
        Mockito.when(commentRepository.save(Mockito.any(Comment.class)))
                .thenAnswer(i -> {
                    // get the first argument passed
                    // in this case we only have one parameter, and it is on place 0
                    Comment commentArgument = i.getArgument(0, Comment.class);
                    return Comment.builder()
                            .author(commentArgument.getAuthor())
                            .gameName(commentArgument.getGameName())
                            .id(1L)
                            .body(commentArgument.getBody())
                            .createdAt(commentArgument.getCreatedAt())
                            .updatedAt(commentArgument.getUpdatedAt())
                            .build();
                });

        // when
        CommentRepresentation commentRepresentation = commentsService.addComment(
                TPUserPrincipal.builder()
                        .id(tPUserStub.getId())
                        .name(tPUserStub.getName())
                        .role(tPUserStub.getRole().name())
                        .updatedAt(tPUserStub.getUpdatedAt().toString())
                        .createdAt(tPUserStub.getCreatedAt().toString())
                        .build(),
                AddCommentRequest.builder()
                        .body("new comment body")
                        .gameName("Snake")
                        .build()
        );

        // then
        assertThat(commentRepresentation.getGameName()).isEqualTo("Snake");
        assertThat(commentRepresentation.getBody()).isEqualTo("new comment body");
    }

    // editComment tests ----------------------------------------------------
    @Test
    void given_1Comment1UserMockedInRepository_when_editCommentIsCalledWithSavedCommentIdAndItsAuthor_then_returnCommentRepresentation() {
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

        Mockito.when(commentRepository.findById(commentStub.getId())).thenReturn(Optional.ofNullable(commentStub));

        // when
        CommentRepresentation commentRepresentation = commentsService.editComment(
                TPUserPrincipal.builder()
                        .name(commentStub.getAuthor().getName())
                        .id(commentStub.getAuthor().getId())
                        .role(Role.USER.name())
                        .createdAt(commentStub.getAuthor().getCreatedAt().toString())
                        .updatedAt(commentStub.getAuthor().getUpdatedAt().toString())
                        .build(),
                commentStub.getId(),
                EditCommentRequest.builder()
                        .newCommentBody("new comment body")
                        .build()
        );

        // then
        assertThat(commentRepresentation.getBody()).isEqualTo("new comment body");
    }

    @Test
    void given_1Comment1UserMockedInRepository_when_editCommentIsCalledWithCommentIdAndUserWhoIsNotItsAuthor_then_throwOperationNotAllowedException() {
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

        Mockito.when(commentRepository.findById(commentStub.getId())).thenReturn(Optional.ofNullable(commentStub));

        // when
        CommentRepresentation commentRepresentation = commentsService.editComment(
                TPUserPrincipal.builder()
                        .name(commentStub.getAuthor().getName())
                        .id(commentStub.getAuthor().getId())
                        .role(Role.USER.name())
                        .createdAt(commentStub.getAuthor().getCreatedAt().toString())
                        .updatedAt(commentStub.getAuthor().getUpdatedAt().toString())
                        .build(),
                commentStub.getId(),
                EditCommentRequest.builder()
                        .newCommentBody("new comment body")
                        .build()
        );

        // then
        OperationNotAllowedException thrown = assertThrows(OperationNotAllowedException.class, () -> commentsService.editComment(
                TPUserPrincipal.builder()
                        .name("unauthorizedUser")
                        .id(123L)
                        .role(Role.USER.name())
                        .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10).toString())
                        .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10).toString())
                        .build(),
                commentStub.getId(),
                EditCommentRequest.builder()
                        .newCommentBody("new comment body")
                        .build())
        );
    }

    @Test
    void given_mockedRepositoryThatReturnsEmptyOptional_when_editCommentIsCalled_then_throwCommentNotFoundException() {
        // given
        Mockito.when(commentRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        // when, then
        CommentNotFoundException thrown = assertThrows(CommentNotFoundException.class, () -> commentsService.editComment(
                TPUserPrincipal.builder()
                        .name("user")
                        .role(Role.USER.name())
                        .id(123L)
                        .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10).toString())
                        .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10).toString())
                        .build(),
                12345L,
                EditCommentRequest.builder()
                        .newCommentBody("new comment body")
                        .build())
        );
    }

    // deleteComment tests --------------------------------------------------
    @Test
    void given_1Comment1UserMockedInRepository_when_deleteCommentIsCalledWithCommentIdAndItsAuthor_then_returnDeleteCommentResponse() {
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

        Mockito.when(commentRepository.findById(commentStub.getId())).thenReturn(Optional.ofNullable(commentStub));
        Mockito.doNothing().when(commentRepository).deleteById(Mockito.any());

        // when
        DeleteCommentResponse deleteCommentResponse = commentsService.deleteComment(
                TPUserPrincipal.builder()
                        .name(commentStub.getAuthor().getName())
                        .id(commentStub.getAuthor().getId())
                        .role(Role.USER.name())
                        .createdAt(commentStub.getAuthor().getCreatedAt().toString())
                        .updatedAt(commentStub.getAuthor().getUpdatedAt().toString())
                        .build(),
                commentStub.getId()
        );

        // then
        assertThat(deleteCommentResponse.getMessage()).isEqualTo("Comment successfully removed");
    }

    @Test
    void given_1Comment2UsersSavedInDB_when_deleteCommentIsCalledWithCommentIdAndUserWhoIsNotItsAuthor_then_operationNotAllowedExceptionsIsThrew() {
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

        Mockito.when(commentRepository.findById(commentStub.getId())).thenReturn(Optional.ofNullable(commentStub));

        // when, then
        OperationNotAllowedException thrown = assertThrows(OperationNotAllowedException.class, () ->
                commentsService.deleteComment(
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
    void given_mockedRepositoryThatReturnsEmptyOptional_when_deleteCommentIsCalled_then_throwCommentNotFoundExceptions() {
        Mockito.when(commentRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        // when, then
        CommentNotFoundException thrown = assertThrows(CommentNotFoundException.class, () ->
                commentsService.deleteComment(
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