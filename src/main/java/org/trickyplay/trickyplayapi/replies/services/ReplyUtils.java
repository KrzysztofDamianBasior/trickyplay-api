package org.trickyplay.trickyplayapi.replies.services;

import org.trickyplay.trickyplayapi.replies.dtos.ReplyRepresentation;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.users.services.UserUtils;

import java.util.List;
import java.util.stream.Collectors;

public class ReplyUtils {
    public static List<ReplyRepresentation> mapToReplyDTOs(List<Reply> replies) {
        return replies.stream()
                .map(ReplyUtils::mapToReplyDTO)
                .collect(Collectors.toList());
    }

    public static ReplyRepresentation mapToReplyDTO(Reply reply) {
        return ReplyRepresentation.builder()
                .id(reply.getId())
                .body(reply.getBody())
                .author(UserUtils.mapToTPUserPublicInfoDTO(reply.getAuthor()))
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }

    private List<Reply> extractRepliesBelongingToComment(List<Reply> replies, long commentId) {
        return replies.stream()
                .filter(reply -> reply.getParentComment().getId() == commentId)
                .collect(Collectors.toList());
    }
}
