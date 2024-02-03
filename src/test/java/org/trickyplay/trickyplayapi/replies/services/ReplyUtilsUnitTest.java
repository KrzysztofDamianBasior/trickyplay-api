package org.trickyplay.trickyplayapi.replies.services;

import org.junit.jupiter.api.Test;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.replies.dtos.ReplyRepresentation;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReplyUtilsUnitTest {
    @Test
    void given_1Comment1User1Reply_when_mapToReplyDTOIsCalled_then_returnCorrespondingReplyRepresentation() {
        TPUser tpUser = TPUser.builder()
                .id(1L)
                .name("testUser")
                .password(("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .id(1L)
                .author(tpUser)
                .gameName("Snake")
                .body("comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply replyStub = Reply.builder()
                .id(1L)
                .author(tpUser)
                .body("reply body")
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .build();

        ReplyRepresentation replyRepresentation = ReplyUtils.mapToReplyDTO(replyStub);

        assertThat(replyRepresentation.getParentComment().getId()).isEqualTo(commentStub.getId());
        assertThat(replyRepresentation.getId()).isEqualTo(replyStub.getId());
        assertThat(replyRepresentation.getBody()).isEqualTo(replyStub.getBody());
        assertThat(replyRepresentation.getCreatedAt()).isEqualTo(replyStub.getCreatedAt());
        assertThat(replyRepresentation.getUpdatedAt()).isEqualTo(replyStub.getUpdatedAt());
        assertThat(replyRepresentation.getAuthor().getId()).isEqualTo(tpUser.getId());
    }

    @Test
    void given_1User1Comment2Replies_when_mapToCommentDTOsIsCalled_then_returnCorrespondingCommentsRepresentationsList() {
        TPUser tpUser = TPUser.builder()
                .id(1L)
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .id(1L)
                .author(tpUser)
                .gameName("Snake")
                .body("earlier comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply firstReplyStub = Reply.builder()
                .id(1L)
                .body("first reply body")
                .parentComment(commentStub)
                .author(tpUser)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .build();

        Reply secondReplyStub = Reply.builder()
                .id(2L)
                .body("second reply body")
                .parentComment(commentStub)
                .author(tpUser)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .build();

        List<Reply> repliesList = List.of(firstReplyStub, secondReplyStub);

        List<ReplyRepresentation> replyRepresentationList = ReplyUtils.mapToReplyDTOs(repliesList);

        assertThat(replyRepresentationList).hasSize(2);
        assertThat(replyRepresentationList.get(0).getAuthor().getId()).isEqualTo(tpUser.getId());
        assertThat(replyRepresentationList.get(0).getParentComment().getId()).isEqualTo(commentStub.getId());
        assertThat(replyRepresentationList.get(0).getId()).isEqualTo(repliesList.get(0).getId());
        assertThat(replyRepresentationList.get(0).getBody()).isEqualTo(repliesList.get(0).getBody());
        assertThat(replyRepresentationList.get(0).getCreatedAt()).isEqualTo(repliesList.get(0).getCreatedAt());
        assertThat(replyRepresentationList.get(0).getUpdatedAt()).isEqualTo(repliesList.get(0).getUpdatedAt());
        assertThat(replyRepresentationList.get(0).getAuthor().getId()).isEqualTo(tpUser.getId());
    }

    @Test
    void given_1User1Comment2Replies_when_extractRepliesThatBelongToCommentIsCalled_then_returnCorrespondingCommentsList() {
        TPUser tpUser = TPUser.builder()
                .id(1L)
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .id(1L)
                .author(tpUser)
                .gameName("Snake")
                .body("earlier comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply firstReplyStub = Reply.builder()
                .id(1L)
                .body("first reply body")
                .parentComment(commentStub)
                .author(tpUser)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .build();

        Reply secondReplyStub = Reply.builder()
                .id(2L)
                .body("second reply body")
                .parentComment(commentStub)
                .author(tpUser)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .build();

        List<Reply> repliesList = List.of(firstReplyStub, secondReplyStub);
        List<Reply> repliesThatBelongToComment = ReplyUtils.extractRepliesThatBelongToComment(repliesList, commentStub.getId());

        assertThat(repliesThatBelongToComment).hasSize(2);
        assertThat(repliesThatBelongToComment).hasSameElementsAs(repliesList);
    }
}