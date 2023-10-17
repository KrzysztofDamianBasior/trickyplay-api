package org.trickyplay.trickyplayapi.comments.services;

import org.trickyplay.trickyplayapi.comments.controllers.CommentsController;
import org.trickyplay.trickyplayapi.comments.dtos.CommentRepresentation;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.users.controllers.UsersController;
import org.trickyplay.trickyplayapi.users.services.UserUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class CommentUtils {
    private CommentUtils() {
    }

    public static List<CommentRepresentation> mapToCommentDTOs(List<Comment> comments) {
        return comments.stream()
                .map(CommentUtils::mapToCommentDTO)
                .collect(Collectors.toList());
    }

    public static CommentRepresentation mapToCommentDTO(Comment comment) {
        CommentRepresentation commentRepresentation = CommentRepresentation.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .gameName(comment.getGameName())
                .author(UserUtils.mapToTPUserPublicInfoDTO(comment.getAuthor()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();

        commentRepresentation.add(linkTo(methodOn(CommentsController.class)
                .getSingleComment(commentRepresentation.getId()))
                .withSelfRel());
        commentRepresentation.add(linkTo(methodOn(UsersController.class)
                .getUser(commentRepresentation.getAuthor().getId()))
                .withRel("author"));
        commentRepresentation.add(linkTo(methodOn(CommentsController.class)
                .getCommentsByGameName(comment.getGameName(), 0, 10, "id", "Asc"))
                .withRel("collection"));
        return commentRepresentation;
    }
}
