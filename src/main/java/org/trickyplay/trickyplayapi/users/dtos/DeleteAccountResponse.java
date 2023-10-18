package org.trickyplay.trickyplayapi.users.dtos;

import lombok.*;

import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DeleteAccountResponse extends RepresentationModel {
    String message;
}
