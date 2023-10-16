package org.trickyplay.trickyplayapi.comments.dtos;

import lombok.*;

import org.trickyplay.trickyplayapi.users.dtos.TPUserPublicInfoDTO;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private long id;
    private String body;
    private String gameName;
    private TPUserPublicInfoDTO author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
