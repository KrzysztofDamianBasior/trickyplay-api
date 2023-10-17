package org.trickyplay.trickyplayapi.replies.dtos;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DeleteReplyResponse extends RepresentationModel {
    String message;
}
