package com.project.fitclub;

import com.project.fitclub.dao.MessageReactionRepository;
import com.project.fitclub.dao.MessageRepository;
import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.*;
import com.project.fitclub.security.JwtTokenProvider;
import com.project.fitclub.security.UserPrincipal;
import com.project.fitclub.security.payload.LoginRequest;
import com.project.fitclub.service.MessageService;
import com.project.fitclub.service.UserService;
import com.project.fitclub.shared.GenericResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
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
    JwtTokenProvider jwtToken;

    @Autowired
    MessageReactionRepository messageReactionRepository;

    @Test
    public void putLike_whenUnauthorizedUser_returns401() {
        String path = String.format(API_1_0_MESSAGES_LIKE, 5);
        ResponseEntity<Object> result = testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void putLike_whenAuthorizedUserForUnknownMessage_returns404() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticate(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);


        String path = String.format(API_1_0_MESSAGES_LIKE, 5);
        ResponseEntity<Object> result = testRestTemplate.exchange(RequestEntity.put(new URI(path)).headers(headers).build(), Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void putLike_whenAuthorizedUserForKnownMessage_savesMessageReactionToDatabase() throws URISyntaxException {
        User inDB = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticate(loggingUser);

        String token = response.getBody().getJwt();
        Message message = messageService.save(inDB, TestUtil.createValidMessage());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        String path = String.format(API_1_0_MESSAGES_LIKE, message.getId());
        testRestTemplate.exchange(RequestEntity.put(new URI(path)).headers(headers).build(), Object.class);

        MessageReaction reaction = messageReactionRepository.findByMessageAndUser(message, inDB);
        assertThat(reaction).isNotNull();
    }

    @Test
    public void putLike_whenAuthorizedUserForKnownMessage_returnsSuccessMessage() {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Message message = messageService.save(user, TestUtil.createValidMessage());

        String path = String.format(API_1_0_MESSAGES_LIKE, message.getId());
        ResponseEntity<GenericResponse> result = testRestTemplate.exchange(path, HttpMethod.PUT, null, GenericResponse.class);
        assertThat(result.getBody().getMessage()).isNotNull();
    }

    @Test
    public void putLike_whenAuthorizedUserAlreadyLikesTheMessage_removesTheReactionFromDatabase() {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
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
    public void putDislike_whenAuthorizedUserForUnknownMessage_returns404() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticate(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        String path = String.format(API_1_0_MESSAGES_DISLIKE, 5);
        ResponseEntity<Object> result = testRestTemplate.exchange(RequestEntity.put(new URI(path)).headers(headers).build(), Object.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void putDislike_whenAuthorizedUserForKnownMessage_savesMessageReactionToDatabase() throws URISyntaxException {
        User inDB = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticate(loggingUser);

        String token = response.getBody().getJwt();

        Message message = messageService.save(inDB, TestUtil.createValidMessage());

        String path = String.format(API_1_0_MESSAGES_DISLIKE, message.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        testRestTemplate.exchange(RequestEntity.put(new URI(path)).headers(headers).build(), GenericResponse.class);

        MessageReaction reaction = messageReactionRepository.findByMessageAndUser(message, inDB);
        assertThat(reaction).isNotNull();
    }

    @Test
    public void putDislike_whenAuthorizedUserForKnownMessage_returnsSuccessMessage() {
        User inDB = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        authenticate(loggingUser);

        Message message = messageService.save(inDB, TestUtil.createValidMessage());

        String path = String.format(API_1_0_MESSAGES_DISLIKE, message.getId());
        ResponseEntity<GenericResponse> result = testRestTemplate.exchange(path, HttpMethod.PUT, null, GenericResponse.class);
        assertThat(result.getBody().getMessage()).isNotNull();
    }

    @Test
    public void putDislike_whenAuthorizedUserAlreadyDislikesTheMessage_removesTheReactionFromDatabase() {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
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
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
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
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
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

    private ResponseEntity<UserPrincipal> authenticate(LoginRequest loggingUser) {
        ResponseEntity<UserPrincipal> userPrincipalResponseEntity = testRestTemplate.postForEntity("/api/1.0/auth/login", loggingUser, UserPrincipal.class);
        return userPrincipalResponseEntity;
    }

    @AfterEach
    public void cleanupAfter() {
        messageReactionRepository.deleteAll();
        messageRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    @BeforeEach
    public void cleanupBefore() {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }
}
