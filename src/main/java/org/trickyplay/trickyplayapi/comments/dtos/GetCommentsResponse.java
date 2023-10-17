package org.trickyplay.trickyplayapi.comments.dtos;

import lombok.*;

import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GetCommentsResponse extends RepresentationModel {
    private long totalElements;
    private int totalPages;
    private int pageSize;
    private int pageNumber;
    private boolean isLast;
    private List<CommentRepresentation> comments;
}
