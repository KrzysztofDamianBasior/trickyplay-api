package org.trickyplay.trickyplayapi.users.services;

import org.junit.jupiter.api.Test;
import org.trickyplay.trickyplayapi.comments.dtos.CommentRepresentation;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.comments.services.CommentUtils;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserUtilsUnitTest {
    @Test
    void given_1Comment1User_when_mapToCommentDTOIsCalled_then_returnCorrespondingCommentRepresentation() {
        // given
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

        // when
        CommentRepresentation commentRepresentation = CommentUtils.mapToCommentDTO(commentStub);

        // then
        assertThat(commentRepresentation.getGameName()).isEqualTo(commentStub.getGameName());
        assertThat(commentRepresentation.getBody()).isEqualTo(commentStub.getBody());
        assertThat(commentRepresentation.getCreatedAt()).isEqualTo(commentStub.getCreatedAt());
        assertThat(commentRepresentation.getUpdatedAt()).isEqualTo(commentStub.getUpdatedAt());
    }

    @Test
    void given_2Comments1User_when_mapToCommentDTOsIsCalled_then_returnCorrespondingCommentsRepresentationsList() {
        // given
        TPUser tpUser = TPUser.builder()
                .id(1L)
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment earlierCommentStub = Comment.builder()
                .id(1L)
                .author(tpUser)
                .gameName("Snake")
                .body("earlier comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Comment laterCommentStub = Comment.builder()
                .id(2L)
                .author(tpUser)
                .gameName("Snake")
                .body("later comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        List<Comment> commentsList = List.of(earlierCommentStub, laterCommentStub);

        // when
        List<CommentRepresentation> commentRepresentationList = CommentUtils.mapToCommentDTOs(commentsList);

        // then
        assertThat(commentRepresentationList).hasSize(2);
        assertThat(commentRepresentationList.get(0).getGameName()).isEqualTo("Snake");
    }
}