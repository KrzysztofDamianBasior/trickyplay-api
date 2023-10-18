package org.trickyplay.trickyplayapi.users.dtos;

import lombok.*;

import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GetUsersResponse extends RepresentationModel {
    private long totalElements;
    private int totalPages;
    private int pageSize;
    private int pageNumber;
    private boolean isLast;
    private List<TPUserRepresentation> users;
}
