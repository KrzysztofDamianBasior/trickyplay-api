package org.trickyplay.trickyplayapi.users.models;

import lombok.Builder;
import lombok.Data;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.trickyplay.trickyplayapi.users.entities.TPUser;

import java.util.Collection;

@Data
@Builder
public class TPUserPrincipal implements UserDetails {
    private TPUser user;

    // private final String name;
    // @JsonIgnore
    // private final String password;
    // private final List<GrantedAuthority> authorities;

    public TPUserPrincipal(TPUser user) {
        this.user = user;

        // private String roles; if we stored the roles in a comma-separated string
        // authorities = Arrays.stream(user.getRoles().split(","))
        //         .map(SimpleGrantedAuthority::new)
        //         .collect(Collectors.toList());
        // but we store roles in an enum
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().getAuthorities();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getName();
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