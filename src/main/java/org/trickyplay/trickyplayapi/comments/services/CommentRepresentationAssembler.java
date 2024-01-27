package org.trickyplay.trickyplayapi.comments.services;

import lombok.Data;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Service;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.trickyplay.trickyplayapi.comments.controllers.CommentsController;
import org.trickyplay.trickyplayapi.comments.dtos.CommentRepresentation;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.users.controllers.UsersController;
import org.trickyplay.trickyplayapi.users.services.UserUtils;

@Data
@Service
public class CommentRepresentationAssembler implements RepresentationModelAssembler<Comment, CommentRepresentation> {

    @Override
    public CommentRepresentation toModel(Comment comment) {
        CommentRepresentation commentRepresentation = CommentRepresentation.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .gameName(comment.getGameName())
                .author(UserUtils.mapToTPUserPublicInfoDTO(comment.getAuthor()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();

        commentRepresentation.add(linkTo(methodOn(CommentsController.class).getSingleComment(commentRepresentation.getId())).withSelfRel());
        commentRepresentation.add(linkTo(methodOn(UsersController.class).getUser(commentRepresentation.getAuthor().getId())).withRel("author"));
        commentRepresentation.add(linkTo(methodOn(CommentsController.class).getCommentsByGameName(comment.getGameName(), 0, 10, "id", "Asc")).withRel("collection"));

        return commentRepresentation;
    }

    @Override
    public CollectionModel<CommentRepresentation> toCollectionModel(Iterable<? extends Comment> comments) {
        CollectionModel<CommentRepresentation> commentsRepresentations = RepresentationModelAssembler.super.toCollectionModel(comments);
        return commentsRepresentations;
    }
}

