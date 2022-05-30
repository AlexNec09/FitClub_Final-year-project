package com.project.fitclub;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.User;
import com.project.fitclub.service.UserService;
import com.project.fitclub.shared.EmailSenderService;
import com.project.fitclub.shared.GenericResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserFollowControllerTest {
    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    User currentUser;
    User userToBeFollowed;
    User userToBeFollowed2;

    @BeforeTransaction
    public void createUsers() {
        currentUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        userToBeFollowed = userService.saveWithoutSendingEmail(TestUtil.createValidUser("target-user"));
        userToBeFollowed2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("target-user-2"));
    }

    @Test
    public void putFollow_whenRequestIsUnauthorized_returns401() {
        ResponseEntity<Object> result = follow(5, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void putFollow_whenAuthorizedUserFollowsNonExistingUser_returns404() {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticate("user1");

        long nonExistingUserId = user.getId() + 5;
        ResponseEntity<Object> result = follow(nonExistingUserId, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void putFollow_whenAuthorizedUserFollowsItself_returns403() {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticate("user1");

        ResponseEntity<Object> result = follow(user.getId(), Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void putFollow_whenAuthorizedUserFollowsAnotherUserForTheFirstTime_returns200() {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        User userToBeFollowed = userService.saveWithoutSendingEmail(TestUtil.createValidUser("target-user"));

        authenticate("user1");

        ResponseEntity<Object> result = follow(userToBeFollowed.getId(), Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Transactional
    public void putFollow_whenAuthorizedUserFollowsAnotherUserForTheFirstTime_followRelationMadeForTheTargetUser() {
        authenticate("user1");

        follow(userToBeFollowed.getId(), Object.class);

        User targetUserInDB = userRepository.findByUsername(userToBeFollowed.getUsername());
        assertThat(targetUserInDB.getFollowedBy()).contains(currentUser);
    }

    @Test
    @Transactional
    public void putFollow_whenAuthorizedUserFollowsAnotherUserForTheFirstTime_followRelationMadeForTheCurrentUser() {
        authenticate("user1");

        follow(userToBeFollowed.getId(), Object.class);

        User currentUserInDB = userRepository.findByUsername(currentUser.getUsername());
        assertThat(currentUserInDB.getFollows()).contains(userToBeFollowed);
    }

    @Test
    @Transactional
    public void putFollow_whenAuthorizedUserFollowsTheUserAlreadyFollowed_relationNotDuplicated() {
        userService.follow(userToBeFollowed.getId(), currentUser.getId());

        authenticate("user1");

        follow(userToBeFollowed.getId(), Object.class);

        User targetUserInDB = userRepository.findByUsername(userToBeFollowed.getUsername());
        assertThat(targetUserInDB.getFollowedBy().size()).isEqualTo(1);
    }

    @Test
    @Transactional
    public void putFollow_whenAuthorizedUserFollowsMultipleUser_followsCollectionCorrectlyUpdated() {
        authenticate("user1");

        follow(userToBeFollowed.getId(), Object.class);

        follow(userToBeFollowed2.getId(), Object.class);

        User currentUserInDB = userRepository.findByUsername(currentUser.getUsername());
        assertThat(currentUserInDB.getFollows().size()).isEqualTo(2);
    }

    @Test
    public void putFollow_whenAuthorizedUserFollowsAnotherUser_returnsGenericResponseWithMessage() {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        User userToBeFollowed = userService.saveWithoutSendingEmail(TestUtil.createValidUser("target-user"));

        authenticate("user1");

        ResponseEntity<GenericResponse> result = follow(userToBeFollowed.getId(), GenericResponse.class);
        assertThat(result.getBody().getMessage()).isNotNull();
    }

    @Test
    public void putUnFollow_whenRequestIsUnauthorized_returns401() {
        ResponseEntity<Object> result = unfollow(5, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    public <T> ResponseEntity<T> unfollow(long userid, Class<T> responseType) throws RestClientException {
        String path = "/api/1.0/users/" + userid + "/unfollow";
        return testRestTemplate.exchange(path, HttpMethod.PUT, null, responseType);
    }

    @Test
    public void putUnfollow_whenAuthorizedUserUnFollowsNonExistingUser_returns404() {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticate("user1");

        long nonExistingUserId = user.getId() + 5;

        ResponseEntity<Object> result = unfollow(nonExistingUserId, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void putUnfollow_whenAuthorizedUserUnFollowsItself_returns403() {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticate("user1");

        ResponseEntity<Object> result = unfollow(user.getId(), Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Transactional
    public void putUnfollow_whenAuthorizedUserUnFollowsTheUserHeFollows_returns200() {
        userService.follow(userToBeFollowed.getId(), currentUser.getId());

        authenticate("user1");

        ResponseEntity<Object> result = unfollow(userToBeFollowed.getId(), Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Transactional
    public void putUnfollow_whenAuthorizedUserUnFollowsTheUserHeFollows_followRelationRemovedForTheTargetUser() {
        authenticate("user1");

        follow(userToBeFollowed.getId(), Object.class);

        unfollow(userToBeFollowed.getId(), Object.class);

        User targetUserInDB = userRepository.findByUsername(userToBeFollowed.getUsername());
        assertThat(targetUserInDB.getFollowedBy()).doesNotContain(currentUser);
    }

    @Test
    @Transactional
    public void putUnfollow_whenAuthorizedUserUnFollowsTheUserHeFollows_followRelationRemovedForTheCurrentUser() {
        authenticate("user1");

        follow(userToBeFollowed.getId(), Object.class);

        unfollow(userToBeFollowed.getId(), Object.class);

        User currentUserInDB = userRepository.findByUsername(currentUser.getUsername());
        assertThat(currentUserInDB.getFollows()).doesNotContain(userToBeFollowed);
    }

    @Test
    @Transactional
    public void putUnfollow_whenAuthorizedUserUnFollowsTheUserAlreadyNotFollowing_relationNotChanged() {
        authenticate("user1");

        unfollow(userToBeFollowed.getId(), Object.class);

        User targetUserInDB = userRepository.findByUsername(userToBeFollowed.getUsername());
        assertThat(targetUserInDB.getFollowedBy().size()).isEqualTo(0);
    }

    @Test
    @Transactional
    public void putUnfollow_whenAuthorizedUserUnFollowsMultipleUser_followsCollectionCorrectlyUpdated() {
        authenticate("user1");

        follow(userToBeFollowed.getId(), Object.class);
        follow(userToBeFollowed2.getId(), Object.class);
        unfollow(userToBeFollowed.getId(), Object.class);
        unfollow(userToBeFollowed2.getId(), Object.class);

        User currentUserInDB = userRepository.findByUsername(currentUser.getUsername());
        assertThat(currentUserInDB.getFollows().size()).isEqualTo(0);
    }

    @Test
    public void putUnfollow_whenAuthorizedUserUnFollowsAnotherUser_returnsGenericResponseWithMessage() {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        User userToBeFollowed = userService.saveWithoutSendingEmail(TestUtil.createValidUser("target-user"));

        authenticate("user1");

        ResponseEntity<GenericResponse> result = unfollow(userToBeFollowed.getId(), GenericResponse.class);
        assertThat(result.getBody().getMessage()).isNotNull();
    }

    private void authenticate(String username) {
        testRestTemplate.getRestTemplate().getInterceptors()
                .add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }

    public <T> ResponseEntity<T> follow(long userid, Class<T> responseType) throws RestClientException {
        String path = "/api/1.0/users/" + userid + "/follow";
        return testRestTemplate.exchange(path, HttpMethod.PUT, null, responseType);
    }

    @AfterEach
    @AfterTransaction
    public void cleanupAfter() {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }
}