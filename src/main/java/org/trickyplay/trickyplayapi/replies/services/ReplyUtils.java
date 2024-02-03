package org.trickyplay.trickyplayapi.replies.services;

import org.trickyplay.trickyplayapi.comments.services.CommentUtils;
import org.trickyplay.trickyplayapi.replies.controllers.RepliesController;
import org.trickyplay.trickyplayapi.replies.dtos.ReplyRepresentation;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.users.controllers.UsersController;
import org.trickyplay.trickyplayapi.users.services.UserUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ReplyUtils {
    public static List<ReplyRepresentation> mapToReplyDTOs(List<Reply> replies) {
        return replies.stream()
                .map(ReplyUtils::mapToReplyDTO)
                .collect(Collectors.toList());
    }

    public static ReplyRepresentation mapToReplyDTO(Reply reply) {
        ReplyRepresentation replyRepresentation = ReplyRepresentation.builder()
                .id(reply.getId())
                .body(reply.getBody())
                .author(UserUtils.mapToTPUserPublicInfoDTO(reply.getAuthor()))
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .parentComment(CommentUtils.mapToCommentDTO(reply.getParentComment()))
                .build();

        replyRepresentation.add(linkTo(methodOn(RepliesController.class)
                .getSingleReply(replyRepresentation.getId()))
                .withSelfRel());
        replyRepresentation.add(linkTo(methodOn(UsersController.class)
                .getUser(replyRepresentation.getAuthor().getId()))
                .withRel("author"));
        replyRepresentation.add(linkTo(methodOn(RepliesController.class)
                .getRepliesByParentCommentId(reply.getParentComment().getId(), 0, 10, "id", "Asc"))
                .withRel("collection"));
        return replyRepresentation;
    }

    static List<Reply> extractRepliesThatBelongToComment(List<Reply> replies, long commentId) {
        return replies.stream()
                .filter(reply -> reply.getParentComment().getId() == commentId)
                .collect(Collectors.toList());
    }
}
