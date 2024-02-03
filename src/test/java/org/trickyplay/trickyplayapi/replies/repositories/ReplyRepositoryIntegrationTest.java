package org.trickyplay.trickyplayapi.replies.repositories;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.trickyplay.trickyplayapi.BaseIntegrationTest;
import org.trickyplay.trickyplayapi.comments.entities.Comment;
import org.trickyplay.trickyplayapi.replies.entities.Reply;
import org.trickyplay.trickyplayapi.users.entities.TPUser;
import org.trickyplay.trickyplayapi.users.enums.Role;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//@Transactional(propagation = Propagation.NOT_SUPPORTED) // If we want to disable transaction management
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// @DataJpaTest can configure databases, will scan for @Entity classes and configure Spring Data JPA repositories. It is also rollback at the end of each test. It provides access to a TestEntityManager bean- an alternative to the regular entity manager
class ReplyRepositoryIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private ReplyRepository replyRepository;

    // TestEntityManager is auto provided and specifically designed to be used in JPA tests. Its methods like persistFlushFind are useful for setting up your data in a test. Otherwise, you would need to call multiple methods when using plain EntityManager. You can use TestEntityManager to insert some data and repository queries to get that same data and then make assertions.
    @Autowired
    private TestEntityManager entityManager; // TestEntityManager provides a subset of EntityManager methods that are useful for tests as well as helper methods for common testing tasks such as persist or find.

    @Test
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    @Test
    void given_1User3Comments4RepliesSavedInDB_when_findAllByParentCommentIdInIsCalled_then_returnCorrespondingRepliesList() {
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

        Reply firstReplyStub = Reply.builder()
                .author(tpUser)
                .body("first comment first reply")
                .parentComment(firstCommentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply secondReplyStub = Reply.builder()
                .author(tpUser)
                .body("first comment second reply")
                .parentComment(firstCommentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply thirdReplyStub = Reply.builder()
                .author(tpUser)
                .body("second comment first reply")
                .parentComment(secondCommentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply fourthReplyStub = Reply.builder()
                .author(tpUser)
                .body("third comment first reply")
                .parentComment(thirdCommentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Long userId = entityManager.persist(tpUser).getId();
        Long firstCommentStubId = entityManager.persist(firstCommentStub).getId();
        Long secondCommentStubId = entityManager.persist(secondCommentStub).getId();
        Long thirdCommentStubId = entityManager.persist(thirdCommentStub).getId();
        Long firstReplyStubId = entityManager.persist(firstReplyStub).getId();
        Long secondReplyStubId = entityManager.persist(secondReplyStub).getId();
        Long thirdReplyStubId = entityManager.persist(thirdReplyStub).getId();
        Long fourthReplyStubId = entityManager.persist(fourthReplyStub).getId();

        //    List<Reply> findAllByParentCommentIdIn(List<Long> ids);
        List<Reply> listOfReplies = replyRepository.findAllByParentCommentIdIn(List.of(firstCommentStubId, secondCommentStubId));

        assertThat(listOfReplies)
                .hasSize(3)
                .hasSameElementsAs(List.of(firstReplyStub, secondReplyStub, thirdReplyStub));
    }

    @Test
    void given_1User2Comments3RepliesSavedInDB_when_findAllIsCalled_then_returnCorrespondingRepliesPage() {
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

        Reply firstReplyStub = Reply.builder()
                .author(tpUser)
                .body("first comment first reply")
                .parentComment(firstCommentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply secondReplyStub = Reply.builder()
                .author(tpUser)
                .body("first comment second reply")
                .parentComment(firstCommentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply thirdReplyStub = Reply.builder()
                .author(tpUser)
                .body("second comment first reply")
                .parentComment(secondCommentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Long userId = entityManager.persist(tpUser).getId();
        Long firstCommentStubId = entityManager.persist(firstCommentStub).getId();
        Long secondCommentStubId = entityManager.persist(secondCommentStub).getId();
        Long firstReplyStubId = entityManager.persist(firstReplyStub).getId();
        Long secondReplyStubId = entityManager.persist(secondReplyStub).getId();
        Long thirdReplyStubId = entityManager.persist(thirdReplyStub).getId();

        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.ASC, "id");

        //    Page<Reply> findAll(Pageable page);
        Page<Reply> repliesPage = replyRepository.findAll(pageable);

        assertThat(repliesPage.getContent())
                .hasSize(3)
                .hasSameElementsAs(List.of(firstReplyStub, secondReplyStub, thirdReplyStub));
        assertThat(repliesPage.getTotalPages()).isEqualTo(1);
        assertThat(repliesPage.isLast()).isTrue();
    }

    @Test
    void given_1User2Comments3RepliesSavedInDB_when_findAllByParentCommentIdIsCalled_then_returnCorrespondingRepliesPage() {
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

        Reply firstReplyStub = Reply.builder()
                .author(tpUser)
                .body("first comment first reply")
                .parentComment(firstCommentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply secondReplyStub = Reply.builder()
                .author(tpUser)
                .body("first comment second reply")
                .parentComment(firstCommentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply thirdReplyStub = Reply.builder()
                .author(tpUser)
                .body("second comment first reply")
                .parentComment(secondCommentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Long userId = entityManager.persist(tpUser).getId();
        Long firstCommentStubId = entityManager.persist(firstCommentStub).getId();
        Long secondCommentStubId = entityManager.persist(secondCommentStub).getId();
        Long firstReplyStubId = entityManager.persist(firstReplyStub).getId();
        Long secondReplyStubId = entityManager.persist(secondReplyStub).getId();
        Long thirdReplyStubId = entityManager.persist(thirdReplyStub).getId();

        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.ASC, "id");

        //    Page<Reply> findAllByParentCommentId(long parentCommentId, Pageable page);
        Page<Reply> repliesPage = replyRepository.findAllByParentCommentId(firstCommentStubId, pageable);

        assertThat(repliesPage.getContent())
                .hasSize(2)
                .hasSameElementsAs(List.of(firstReplyStub, secondReplyStub));
        assertThat(repliesPage.getTotalPages()).isEqualTo(1);
        assertThat(repliesPage.isLast()).isTrue();
    }

    @Test
    void given_2Users1Comment3RepliesSavedInDB_when_findAllByAuthorNameIsCalled_then_returnCorrespondingResultsPage() {
        String firstAuthorName = "testUser1";
        String secondAuthorName = "testUser2";
        TPUser firstAuthor = TPUser.builder()
                .name(firstAuthorName)
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser secondAuthor = TPUser.builder()
                .name(secondAuthorName)
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(firstAuthor)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply firstReplyStub = Reply.builder()
                .author(secondAuthor)
                .body("first reply")
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply secondReplyStub = Reply.builder()
                .author(secondAuthor)
                .body("second reply")
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply thirdReplyStub = Reply.builder()
                .author(firstAuthor)
                .body("third reply")
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Long firstAuthorId = entityManager.persist(firstAuthor).getId();
        Long secondAuthorId = entityManager.persist(secondAuthor).getId();
        Long commentStubId = entityManager.persist(commentStub).getId();
        Long firstReplyStubId = entityManager.persist(firstReplyStub).getId();
        Long secondReplyStubId = entityManager.persist(secondReplyStub).getId();
        Long thirdReplyStubId = entityManager.persist(thirdReplyStub).getId();

        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.ASC, "id");

        //    Page<Reply> findAllByAuthorName(String authorName, Pageable page);
        Page<Reply> repliesPage = replyRepository.findAllByAuthorName(secondAuthorName, pageable);

        assertThat(repliesPage.getContent())
                .hasSize(2)
                .hasSameElementsAs(List.of(firstReplyStub, secondReplyStub));
        assertThat(repliesPage.getTotalPages()).isEqualTo(1);
        assertThat(repliesPage.isLast()).isTrue();
    }

    @Test
    void given_1User1Comment3Replies_when_findAllByAuthorIdIsCalled_then_returnCorrespondingResultsPage() {
        String firstAuthorName = "testUser1";
        String secondAuthorName = "testUser2";
        TPUser firstAuthor = TPUser.builder()
                .name(firstAuthorName)
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        TPUser secondAuthor = TPUser.builder()
                .name(secondAuthorName)
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(firstAuthor)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply firstReplyStub = Reply.builder()
                .author(secondAuthor)
                .body("first reply")
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply secondReplyStub = Reply.builder()
                .author(secondAuthor)
                .body("second reply")
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply thirdReplyStub = Reply.builder()
                .author(firstAuthor)
                .body("third reply")
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Long firstAuthorId = entityManager.persist(firstAuthor).getId();
        Long secondAuthorId = entityManager.persist(secondAuthor).getId();
        Long commentStubId = entityManager.persist(commentStub).getId();
        Long firstReplyStubId = entityManager.persist(firstReplyStub).getId();
        Long secondReplyStubId = entityManager.persist(secondReplyStub).getId();
        Long thirdReplyStubId = entityManager.persist(thirdReplyStub).getId();

        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.ASC, "id");

        //    Page<Reply> findAllByAuthorId(long id, Pageable page);
        Page<Reply> repliesPage = replyRepository.findAllByAuthorId(secondAuthorId, pageable);

        assertThat(repliesPage.getContent())
                .hasSize(2)
                .hasSameElementsAs(List.of(firstReplyStub, secondReplyStub));
        assertThat(repliesPage.getTotalPages()).isEqualTo(1);
        assertThat(repliesPage.isLast()).isTrue();
    }

    @Test
    void given_1User1Comment3Replies_when_findAllByIdInIsCalled_then_returnCorrespondingResultsPage() {
        TPUser tpUser = TPUser.builder()
                .name("testUser")
                .password("123TestUserPassword")
                .role(Role.USER)
                .refreshTokens(null)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(10))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusDays(9))
                .build();

        Comment commentStub = Comment.builder()
                .author(tpUser)
                .gameName("Snake")
                .body("first comment body")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(4))
                .build();

        Reply firstReplyStub = Reply.builder()
                .author(tpUser)
                .body("first reply")
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply secondReplyStub = Reply.builder()
                .author(tpUser)
                .body("second reply")
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply thirdReplyStub = Reply.builder()
                .author(tpUser)
                .body("third reply")
                .parentComment(commentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Long tpUserId = entityManager.persist(tpUser).getId();
        Long commentStubId = entityManager.persist(commentStub).getId();
        Long firstReplyStubId = entityManager.persist(firstReplyStub).getId();
        Long secondReplyStubId = entityManager.persist(secondReplyStub).getId();
        Long thirdReplyStubId = entityManager.persist(thirdReplyStub).getId();

        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.ASC, "id");

        //    List<Reply> findAllByIdIn(List<Long> ids); // where x.id in ?1
        List<Reply> repliesList = replyRepository.findAllByIdIn(List.of(firstReplyStubId, secondReplyStubId));

        assertThat(repliesList)
                .hasSize(2)
                .hasSameElementsAs(List.of(firstReplyStub, secondReplyStub));
    }

    @Test
    void  given_1User2Comments3RepliesSavedInDB_when_findAllRepliesWithAuthorsIsCalled_then_returnCorrespondingRepliesPage() {
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

        Reply firstReplyStub = Reply.builder()
                .author(tpUser)
                .body("first comment first reply")
                .parentComment(firstCommentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply secondReplyStub = Reply.builder()
                .author(tpUser)
                .body("first comment second reply")
                .parentComment(firstCommentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Reply thirdReplyStub = Reply.builder()
                .author(tpUser)
                .body("second comment first reply")
                .parentComment(secondCommentStub)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .build();

        Long userId = entityManager.persist(tpUser).getId();
        Long firstCommentStubId = entityManager.persist(firstCommentStub).getId();
        Long secondCommentStubId = entityManager.persist(secondCommentStub).getId();
        Long firstReplyStubId = entityManager.persist(firstReplyStub).getId();
        Long secondReplyStubId = entityManager.persist(secondReplyStub).getId();
        Long thirdReplyStubId = entityManager.persist(thirdReplyStub).getId();

        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.ASC, "id");

//        @Query("Select r from Reply r left join fetch r.author")
//        Page<Reply> findAllRepliesWithAuthors(Pageable page);
        Page<Reply> repliesPage = replyRepository.findAllRepliesWithAuthors(pageable);

        assertThat(repliesPage.getContent())
                .hasSize(3)
                .hasSameElementsAs(List.of(firstReplyStub, secondReplyStub, thirdReplyStub));
        assertThat(repliesPage.getTotalPages()).isEqualTo(1);
        assertThat(repliesPage.isLast()).isTrue();
    }
}