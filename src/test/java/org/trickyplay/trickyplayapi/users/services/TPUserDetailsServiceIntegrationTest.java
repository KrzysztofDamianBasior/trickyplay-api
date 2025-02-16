package org.trickyplay.trickyplayapi.users.services;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.models.TPUserPrincipal;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SpringBootTest
class TPUserDetailsServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private TPUserRepository tPUserRepository;
    @Autowired
    private TPUserDetailsService tpUserDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    // loadUserByUsername tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_loadUserByUsernameIsCalled_then_returnCorrespondingUserDetails() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        // when
        UserDetails userDetails = tpUserDetailsService.loadUserByUsername(tPUserSaved.getName());

        // then
        assertThat(userDetails.getUsername()).isEqualTo(tPUserStub.getName());
        assertThat(userDetails.getPassword()).isEqualTo(tPUserStub.getPassword());
        assertThat(((TPUserPrincipal)userDetails).getAuthorities()).hasSameElementsAs(Role.USER.getAuthorities());
    }

    // loadUserById tests -----------------------------------------------
    @Test
    @Transactional
    void given_1UserSavedInDB_when_loadUserByIdIsCalled_then_returnCorrespondingUserDetails() {
        // given
        TPUser tPUserStub = TPUser.builder()
                .name("user")
                .password(passwordEncoder.encode("123TestUserPassword"))
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();
        TPUser tPUserSaved = tPUserRepository.save(tPUserStub);

        // when
        UserDetails userDetails = tpUserDetailsService.loadUserById(tPUserSaved.getId());

        // then
        assertThat(userDetails.getUsername()).isEqualTo(tPUserStub.getName());
        assertThat(userDetails.getPassword()).isEqualTo(tPUserStub.getPassword());
        assertThat(((TPUserPrincipal)userDetails).getAuthorities()).hasSameElementsAs(Role.USER.getAuthorities());
    }
}