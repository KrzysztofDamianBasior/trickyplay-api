package org.trickyplay.trickyplayapi.comments.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;

//@Transactional(propagation = Propagation.NOT_SUPPORTED) // If we want to disable transaction management
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// @DataJpaTest can configure databases, will scan for @Entity classes and configure Spring Data JPA repositories. It is also rollback at the end of each test. It provides access to a TestEntityManager bean- an alternative to the regular entity manager
class CommentRepositoryIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private CommentRepository commentRepository;

    // TestEntityManager is auto provided and specifically designed to be used in JPA tests. Its methods like persistFlushFind are useful for setting up your data in a test. Otherwise, you would need to call multiple methods when using plain EntityManager. You can use TestEntityManager to insert some data and repository queries to get that same data and then make assertions.
    @Autowired
    private TestEntityManager entityManager; // TestEntityManager provides a subset of EntityManager methods that are useful for tests as well as helper methods for common testing tasks such as persist or find.

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    @Test
    void given_1User3CommentsSavedInDB_when_findAllIsCalled_then_returnCorrespondingCommentsPage() {
        TPUser tpUser = TPUser.builder()
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment firstCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Comment secondCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("second comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Comment thirdCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("third comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Long userId = entityManager.persist(tpUser).getId();
        Long firstCommentStubId = entityManager.persist(firstCommentStub).getId();
        Long secondCommentStubId = entityManager.persist(secondCommentStub).getId();
        Long thirdCommentStubId = entityManager.persist(thirdCommentStub).getId();

        // When you use EntityManager.persist(), the entity is added to the Persistence Context and managed by the EntityManager. However, the data is not immediately persisted to the database. Instead, it is persisted when the transaction is committed. On the other hand, when you use EntityManager.flush(), the entity is immediately persisted to the database, regardless of the transaction status. The FlushModeType configuration of the EntityManager determines the behavior of EntityManager.flush(). By default, FlushModeType is set to AUTO, which means that a flush will be done automatically. But if it’s set to COMMIT, the persistence of the data to the underlying database will be delayed until the transaction is committed.
        entityManager.flush(); // if you use EntityManager.flush(), the data is persisted immediately, regardless of the transaction status

        Pageable pageable = PageRequest.of(0, 2, Sort.Direction.ASC, "id");
        Page<Comment> commentsPage = commentRepository.findAll(pageable);
        List<Comment> listOfStubs = List.of(firstCommentStub, secondCommentStub, thirdCommentStub);

        assertThat(commentsPage.getContent())
                .contains(firstCommentStub)
                .contains(secondCommentStub)
                .hasSize(2)
                .doesNotHaveDuplicates()
                .doesNotContainNull();
        assertThat(commentsPage.getTotalPages())
                .isEqualTo(2);
        assertThat(commentsPage.getTotalElements())
                .isEqualTo(3);
        assertThat(commentsPage.isLast())
                .isFalse();
    }

    @Test
    void given_1User2CommentsSavedInDB_when_findAllByAuthorIdIsCalled_then_returnCorrespondingCommentsPage() {
        TPUser tpUser = TPUser.builder()
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment earlierCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("earlier comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Comment laterCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("later comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Long userId = entityManager.persist(tpUser).getId();
        Long earlierCommentStubId = entityManager.persist(earlierCommentStub).getId();
        Long laterCommentStubId = entityManager.persist(laterCommentStub).getId();

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "id");

        Page<Comment> commentsPage = commentRepository.findAllByAuthorId(userId, pageable);
        List<Comment> listOfStubs = List.of(earlierCommentStub, laterCommentStub);

        assertThat(commentsPage.getContent())
                .contains(earlierCommentStub)
                .contains(laterCommentStub)
                .hasSize(2)
                .doesNotHaveDuplicates()
                .doesNotContainNull()
                .hasSameElementsAs(listOfStubs);
        assertThat(commentsPage.getTotalPages())
                .isEqualTo(1);
        assertThat(commentsPage.getTotalElements())
                .isEqualTo(2);
        assertThat(commentsPage.isLast()).isTrue();
    }

    @Test
    void given_1User2CommentsSavedInDB_when_findAllByAuthorNameIsCalled_then_returnCorrespondingCommentsPage() {
        TPUser tpUser = TPUser.builder()
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment earlierCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("earlier comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Comment laterCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("later comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        String userName = entityManager.persist(tpUser).getName();
        Long earlierCommentStubId = entityManager.persist(earlierCommentStub).getId();
        Long laterCommentStubId = entityManager.persist(laterCommentStub).getId();

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "id");

        Page<Comment> commentsPage = commentRepository.findAllByAuthorName(userName, pageable);
        List<Comment> listOfStubs = List.of(earlierCommentStub, laterCommentStub);

        assertThat(commentsPage.getContent())
                .contains(earlierCommentStub)
                .contains(laterCommentStub)
                .hasSize(2)
                .doesNotHaveDuplicates()
                .doesNotContainNull()
                .hasSameElementsAs(listOfStubs);
        assertThat(commentsPage.getTotalPages())
                .isEqualTo(1);
        assertThat(commentsPage.getTotalElements())
                .isEqualTo(2);
        assertThat(commentsPage.isLast()).isTrue();
    }

    @Test
    void given_1User3CommentsSavedInDB_when_findAllByGameNameIsCalled_then_returnCorrespondingCommentsPage() {
        Pageable pageable = PageRequest.of(0, 2, Sort.Direction.ASC, "id");

        TPUser tpUser = TPUser.builder()
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment firstCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Comment secondCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("second comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Comment thirdCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("TicTacToe")
                .body("third comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Long userId = entityManager.persist(tpUser).getId();
        Long firstCommentStubId = entityManager.persist(firstCommentStub).getId();
        Long secondCommentStubId = entityManager.persist(secondCommentStub).getId();
        Long thirdCommentStubId = entityManager.persist(thirdCommentStub).getId();

        Page<Comment> commentsPage = commentRepository.findAllByGameName("Snake", pageable);
        List<Comment> listOfStubs = List.of(firstCommentStub, secondCommentStub, thirdCommentStub);

        assertThat(commentsPage.getContent())
                .contains(firstCommentStub)
                .contains(secondCommentStub)
                .hasSize(2)
                .doesNotHaveDuplicates();
        assertThat(commentsPage.getTotalPages())
                .isEqualTo(1);
        assertThat(commentsPage.getTotalElements())
                .isEqualTo(2);
        assertThat(commentsPage.isLast())
                .isTrue();
    }

    @Test
    void given_1User3CommentsSavedInDB_when_findAllByIdInIsCalled_then_returnCorrespondingCommentsList() {
        TPUser tpUser = TPUser.builder()
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment firstCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Comment secondCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("second comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Comment thirdCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("TicTacToe")
                .body("third comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Long userId = entityManager.persist(tpUser).getId();
        Long firstCommentStubId = entityManager.persist(firstCommentStub).getId();
        Long secondCommentStubId = entityManager.persist(secondCommentStub).getId();
        Long thirdCommentStubId = entityManager.persist(thirdCommentStub).getId();

        List<Comment> commentsList = commentRepository.findAllByIdIn(List.of(firstCommentStubId, secondCommentStubId));

        assertThat(commentsList.size()).isEqualTo(2);
        assertThat(commentsList)
                .contains(firstCommentStub)
                .contains(secondCommentStub)
                .hasSize(2)
                .doesNotHaveDuplicates()
                .doesNotContainNull();
    }

    @Test
    void given_1User3CommentsSavedInDB_when_findAllCommentsWithAuthorsIsCalled_then_returnCorrespondingCommentsPage() {
        Pageable pageable = PageRequest.of(0, 2, Sort.Direction.ASC, "id");

        TPUser tpUser = TPUser.builder()
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment firstCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Comment secondCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("second comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Comment thirdCommentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("third comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Long userId = entityManager.persist(tpUser).getId();
        Long firstCommentStubId = entityManager.persist(firstCommentStub).getId();
        Long secondCommentStubId = entityManager.persist(secondCommentStub).getId();
        Long thirdCommentStubId = entityManager.persist(thirdCommentStub).getId();

        Page<Comment> commentsPage = commentRepository.findAllCommentsWithAuthors(pageable);

        assertThat(commentsPage.getContent())
                .contains(firstCommentStub)
                .contains(secondCommentStub)
                .hasSize(2)
                .doesNotHaveDuplicates()
                .doesNotContainNull();
        assertThat(commentsPage.getTotalPages())
                .isEqualTo(2);
        assertThat(commentsPage.getTotalElements())
                .isEqualTo(3);
        assertThat(commentsPage.isLast())
                .isFalse();
    }
}
