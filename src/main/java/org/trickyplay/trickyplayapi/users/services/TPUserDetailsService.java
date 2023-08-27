package org.trickyplay.trickyplayapi.users.services;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

@Service
@RequiredArgsConstructor
public class TPUserDetailsService implements UserDetailsService {
    private final TPUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        // UsernameNotFoundException which is a subclass of AuthenticationException is thrown during authenticaton
        TPUser user = userRepository.findByName(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new TPUserPrincipal(user);

        // UserDetails user =
        //         User.withUsername(customer.getEmail()).password(customer.getPassword()).authorities("USER").build();
        // return user;
        //

        // return TPUserPrincipal.builder()
        //        .userId(user.getId())
        //        .email(user.getEmail())
        //        .password(user.getPassword())
        //        .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
        //        .build();
    }
}