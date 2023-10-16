package org.trickyplay.trickyplayapi.comments.dtos;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCommentResponse {
    private String message;
}
