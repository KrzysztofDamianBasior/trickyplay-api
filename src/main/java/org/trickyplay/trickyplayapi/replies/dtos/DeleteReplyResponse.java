package org.trickyplay.trickyplayapi.replies.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DeleteReplyResponse {
    String message;
}