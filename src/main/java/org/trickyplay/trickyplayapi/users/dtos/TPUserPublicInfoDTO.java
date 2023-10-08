package org.trickyplay.trickyplayapi.users.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.trickyplay.trickyplayapi.users.enums.Role;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class TPUserPublicInfoDTO {
    private Long id;

    private String name;

    private Role role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
