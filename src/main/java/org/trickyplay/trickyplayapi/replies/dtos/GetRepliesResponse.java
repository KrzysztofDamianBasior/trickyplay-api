package org.trickyplay.trickyplayapi.replies.dtos;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GetRepliesResponse extends RepresentationModel {
    private long totalElements;
    private int totalPages;
    private int pageSize;
    private int pageNumber;
    private boolean isLast;
    private List<ReplyRepresentation> replies;
}
