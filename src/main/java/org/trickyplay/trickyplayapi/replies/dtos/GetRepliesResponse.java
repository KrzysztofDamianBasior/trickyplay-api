package org.trickyplay.trickyplayapi.replies.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetRepliesResponse {
    private long totalElements;
    private int totalPages;
    private int pageSize;
    private int pageNumber;
    private boolean isLast;
    private List<ReplyDTO> replies;
}
