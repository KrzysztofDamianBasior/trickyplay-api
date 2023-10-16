package org.trickyplay.trickyplayapi.replies.dtos;

import lombok.*;

import org.trickyplay.trickyplayapi.users.dtos.TPUserPublicInfoDTO;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDTO {
    private Long id;
    private String body;
    private TPUserPublicInfoDTO author;
    private LocalDateTime createdAt; //ISO-8601 UTC
    private LocalDateTime updatedAt; //ISO-8601 UTC
}
