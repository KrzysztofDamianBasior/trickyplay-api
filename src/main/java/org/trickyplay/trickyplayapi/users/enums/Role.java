package org.trickyplay.trickyplayapi.users.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.trickyplay.trickyplayapi.users.enums.Permission.ADMIN_READ;
import static org.trickyplay.trickyplayapi.users.enums.Permission.ADMIN_UPDATE;
import static org.trickyplay.trickyplayapi.users.enums.Permission.ADMIN_CREATE;
import static org.trickyplay.trickyplayapi.users.enums.Permission.ADMIN_DELETE;
import static org.trickyplay.trickyplayapi.users.enums.Permission.USER_READ;
import static org.trickyplay.trickyplayapi.users.enums.Permission.USER_UPDATE;
import static org.trickyplay.trickyplayapi.users.enums.Permission.USER_CREATE;
import static org.trickyplay.trickyplayapi.users.enums.Permission.USER_DELETE;

@RequiredArgsConstructor
public enum Role {
    BANNED(Collections.emptySet()), ADMIN(Set.of(ADMIN_READ, ADMIN_UPDATE, ADMIN_DELETE, ADMIN_CREATE, USER_READ, USER_UPDATE, USER_DELETE, USER_CREATE)), USER(Set.of(USER_READ, USER_UPDATE, USER_DELETE, USER_CREATE));

    @Getter
    private final Set<Permission> permissions; // allowedOperations

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());

        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
