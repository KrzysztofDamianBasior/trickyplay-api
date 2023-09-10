package org.trickyplay.trickyplayapi.users.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;

import java.util.Collection;

@Data
public class TPUserPrincipal implements UserDetails {
    private final Long id;
    private final String name;
    @JsonIgnore     // protects against accidental serialization of a sensitive field
    private final String password;

    private final Role role;
//  alternatively- private final List<GrantedAuthority> authorities;

    @Builder
    public TPUserPrincipal(Long id, String name, String password, String role) {
        this.id = id;
        this.role = Role.valueOf(role); // that the name must be an exact match, or else it throws an IllegalArgumentException
        this.name = name;
        this.password = password;
    }

    @Builder
    public TPUserPrincipal(TPUser user) {
        this.name = user.getName();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.id = user.getId();

        // if we kept the roles in a comma-separated string
        // this.authorities = Arrays.stream(user.getRoles().split(","))
        //         .map(SimpleGrantedAuthority::new)
        //         .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}