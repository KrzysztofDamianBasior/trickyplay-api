package org.trickyplay.trickyplayapi.users.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.assertj.core.api.Assertions.assertThat;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;

import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TPUserDetailsServiceUnitTest {
    @Mock
    private TPUserRepository tPUserRepository;
    @InjectMocks
    private TPUserDetailsService tPUserDetailsService;

    // loadUserByUsername tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_loadUserByUsernameIsCalled_then_returnCorrespondingUserDetails() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Mockito.when(tPUserRepository.findByName(tPUserStub.getName())).thenReturn(Optional.ofNullable(tPUserStub));

        // when
        UserDetails userDetails = tPUserDetailsService.loadUserByUsername(tPUserStub.getName());

        // then
        assertThat(userDetails.getUsername()).isEqualTo(tPUserStub.getName());
        assertThat(userDetails.getPassword()).isEqualTo(tPUserStub.getPassword());
        assertThat(((TPUserPrincipal)userDetails).getAuthorities()).hasSameElementsAs(Role.USER.getAuthorities());
    }

    // loadUserById tests -----------------------------------------------
    @Test
    void given_1UserMockedInRepository_when_loadUserByIdIsCalled_then_returnCorrespondingUserDetails() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .id(1L)
                .name("user")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Mockito.when(tPUserRepository.findById(tPUserStub.getId())).thenReturn(Optional.ofNullable(tPUserStub));

        // when
        UserDetails userDetails = tPUserDetailsService.loadUserById(tPUserStub.getId());

        // then
        assertThat(userDetails.getUsername()).isEqualTo(tPUserStub.getName());
        assertThat(userDetails.getPassword()).isEqualTo(tPUserStub.getPassword());
        assertThat(((TPUserPrincipal)userDetails).getAuthorities()).hasSameElementsAs(Role.USER.getAuthorities());
    }
}