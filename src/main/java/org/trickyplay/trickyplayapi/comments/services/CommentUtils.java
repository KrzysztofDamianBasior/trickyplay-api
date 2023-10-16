package org.trickyplay.trickyplayapi.comments.services;

import org.trickyplay.trickyplayapi.comments.dtos.CommentDTO;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.users.services.UserUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CommentUtils {
    private CommentUtils() {
    }

    public static List<CommentDTO> mapToCommentDTOs(List<Comment> comments) {
        return comments.stream()
                .map(CommentUtils::mapToCommentDTO)
                .collect(Collectors.toList());
    }

    public static CommentDTO mapToCommentDTO(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .gameName(comment.getGameName())
                .author(UserUtils.mapToTPUserPublicInfoDTO(comment.getAuthor()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
