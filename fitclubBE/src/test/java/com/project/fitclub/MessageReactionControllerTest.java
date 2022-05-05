package com.project.fitclub;

import com.project.fitclub.dao.MessageReactionRepository;
import com.project.fitclub.dao.MessageRepository;
import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.Message;
import com.project.fitclub.model.MessageReaction;
import com.project.fitclub.model.Reaction;
import com.project.fitclub.model.User;
import com.project.fitclub.service.MessageService;
import com.project.fitclub.service.UserService;
import com.project.fitclub.shared.GenericResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MessageReactionControllerTest {

    public static final String API_1_0_MESSAGES_LIKE = "/api/1.0/messages/%d/like";

    public static final String API_1_0_MESSAGES_DISLIKE = "/api/1.0/messages/%d/dislike";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageService messageService;

    @Autowired
    MessageReactionRepository messageReactionRepository;

    @Test
    public void putLike_whenUnauthorizedUser_returns401() {
        String path = String.format(API_1_0_MESSAGES_LIKE, 5);
        ResponseEntity<Object> result = testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void putLike_whenAuthorizedUserForUnknownMessage_returns404() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        String path = String.format(API_1_0_MESSAGES_LIKE, 5);
        ResponseEntity<Object> result = testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void putLike_whenAuthorizedUserForKnownMessage_savesMessageReactionToDatabase() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Message message = messageService.save(user, TestUtil.createValidMessage());

        String path = String.format(API_1_0_MESSAGES_LIKE, message.getId());
        testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);

        MessageReaction reaction = messageReactionRepository.findByMessageAndUser(message, user);
        assertThat(reaction).isNotNull();
    }

    @Test
    public void putLike_whenAuthorizedUserForKnownMessage_returnsSuccessMessage() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Message message = messageService.save(user, TestUtil.createValidMessage());

        String path = String.format(API_1_0_MESSAGES_LIKE, message.getId());
        ResponseEntity<GenericResponse> result = testRestTemplate.exchange(path, HttpMethod.PUT, null, GenericResponse.class);
        assertThat(result.getBody().getMessage()).isNotNull();
    }

    @Test
    public void putLike_whenAuthorizedUserAlreadyLikesTheMessage_removesTheReactionFromDatabase() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Message message = messageService.save(user, TestUtil.createValidMessage());

        String path = String.format(API_1_0_MESSAGES_LIKE, message.getId());
        testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);

        testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);

        MessageReaction reaction = messageReactionRepository.findByMessageAndUser(message, user);
        assertThat(reaction).isNull();
    }

    @Test
    public void putDislike_whenUnauthorizedUser_returns401() {
        String path = String.format(API_1_0_MESSAGES_DISLIKE, 5);
        ResponseEntity<Object> result = testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void putDislike_whenAuthorizedUserForUnknownMessage_returns404() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        String path = String.format(API_1_0_MESSAGES_DISLIKE, 5);
        ResponseEntity<Object> result = testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void putDislike_whenAuthorizedUserForKnownMessage_savesMessageReactionToDatabase() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Message message = messageService.save(user, TestUtil.createValidMessage());

        String path = String.format(API_1_0_MESSAGES_DISLIKE, message.getId());
        testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);

        MessageReaction reaction = messageReactionRepository.findByMessageAndUser(message, user);
        assertThat(reaction).isNotNull();
    }

    @Test
    public void putDislike_whenAuthorizedUserForKnownMessage_returnsSuccessMessage() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Message message = messageService.save(user, TestUtil.createValidMessage());

        String path = String.format(API_1_0_MESSAGES_DISLIKE, message.getId());
        ResponseEntity<GenericResponse> result = testRestTemplate.exchange(path, HttpMethod.PUT, null, GenericResponse.class);
        assertThat(result.getBody().getMessage()).isNotNull();
    }

    @Test
    public void putDislike_whenAuthorizedUserAlreadyDislikesTheMessage_removesTheReactionFromDatabase() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Message message = messageService.save(user, TestUtil.createValidMessage());

        String path = String.format(API_1_0_MESSAGES_DISLIKE, message.getId());
        testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);

        testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);

        MessageReaction reaction = messageReactionRepository.findByMessageAndUser(message, user);
        assertThat(reaction).isNull();
    }

    @Test
    public void putDislike_whenAuthorizedUserAlreadyLikesTheMessage_updatesTheReactionInDatabase() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Message message = messageService.save(user, TestUtil.createValidMessage());

        String pathLike = String.format(API_1_0_MESSAGES_LIKE, message.getId());
        testRestTemplate.exchange(pathLike, HttpMethod.PUT, null, Object.class);

        String pathDislike = String.format(API_1_0_MESSAGES_DISLIKE, message.getId());
        testRestTemplate.exchange(pathDislike, HttpMethod.PUT, null, Object.class);

        MessageReaction reaction = messageReactionRepository.findByMessageAndUser(message, user);
        assertThat(reaction.getReaction()).isEqualTo(Reaction.DISLIKE);
    }

    @Test
    public void putLike_whenAuthorizedUserAlreadyDislikesTheMessage_updatesTheReactionInDatabase() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Message message = messageService.save(user, TestUtil.createValidMessage());

        String pathDislike = String.format(API_1_0_MESSAGES_DISLIKE, message.getId());
        testRestTemplate.exchange(pathDislike, HttpMethod.PUT, null, Object.class);

        String pathLike = String.format(API_1_0_MESSAGES_LIKE, message.getId());
        testRestTemplate.exchange(pathLike, HttpMethod.PUT, null, Object.class);

        MessageReaction reaction = messageReactionRepository.findByMessageAndUser(message, user);
        assertThat(reaction.getReaction()).isEqualTo(Reaction.LIKE);
    }

    private void authenticateUser(String username) {
        testRestTemplate.getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }

    @AfterEach
    public void cleanupAfter() {
        messageReactionRepository.deleteAll();
        messageRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }
}
