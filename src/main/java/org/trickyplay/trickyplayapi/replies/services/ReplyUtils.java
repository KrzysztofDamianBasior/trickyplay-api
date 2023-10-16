package org.trickyplay.trickyplayapi.replies.services;

import org.trickyplay.trickyplayapi.replies.dtos.ReplyDTO;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.users.services.UserUtils;

import java.util.List;
import java.util.stream.Collectors;

public class ReplyUtils {
    public static List<ReplyDTO> mapToReplyDTOs(List<Reply> replies) {
        return replies.stream()
                .map(ReplyUtils::mapToReplyDTO)
                .collect(Collectors.toList());
    }

    public static ReplyDTO mapToReplyDTO(Reply reply) {
        return ReplyDTO.builder()
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
