package org.trickyplay.trickyplayapi.comments.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetCommentsResponse {
    private long totalElements;
    private int totalPages;
    private int pageSize;
    private int pageNumber;
    private boolean isLast;
    private List<CommentDTO> comments;
}
