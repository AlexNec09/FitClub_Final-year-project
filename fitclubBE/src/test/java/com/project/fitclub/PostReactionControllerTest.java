package com.project.fitclub;

import com.project.fitclub.dao.PostReactionRepository;
import com.project.fitclub.dao.PostRepository;
import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.*;
import com.project.fitclub.security.JwtTokenProvider;
import com.project.fitclub.security.UserPrincipal;
import com.project.fitclub.security.payload.LoginRequest;
import com.project.fitclub.service.PostService;
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
public class PostReactionControllerTest {

    public static final String API_1_0_POSTS_LIKE = "/api/1.0/posts/%d/like";

    public static final String API_1_0_POSTS_DISLIKE = "/api/1.0/posts/%d/dislike";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostService postService;

    @Autowired
    JwtTokenProvider jwtToken;

    @Autowired
    PostReactionRepository postReactionRepository;

    @Test
    public void putLike_whenUnauthorizedUser_returns401() {
        String path = String.format(API_1_0_POSTS_LIKE, 5);
        ResponseEntity<Object> result = testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void putLike_whenAuthorizedUserForUnknownPost_returns404() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticate(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);


        String path = String.format(API_1_0_POSTS_LIKE, 5);
        ResponseEntity<Object> result = testRestTemplate.exchange(RequestEntity.put(new URI(path)).headers(headers).build(), Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void putLike_whenAuthorizedUserForKnownPost_savesPostReactionToDatabase() throws URISyntaxException {
        User inDB = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticate(loggingUser);

        String token = response.getBody().getJwt();
        Post post = postService.save(inDB, TestUtil.createValidPost());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        String path = String.format(API_1_0_POSTS_LIKE, post.getId());
        testRestTemplate.exchange(RequestEntity.put(new URI(path)).headers(headers).build(), Object.class);

        PostReaction reaction = postReactionRepository.findByPostAndUser(post, inDB);
        assertThat(reaction).isNotNull();
    }

    @Test
    public void putLike_whenAuthorizedUserForKnownPost_returnsSuccessPost() {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Post post = postService.save(user, TestUtil.createValidPost());

        String path = String.format(API_1_0_POSTS_LIKE, post.getId());
        ResponseEntity<GenericResponse> result = testRestTemplate.exchange(path, HttpMethod.PUT, null, GenericResponse.class);
        assertThat(result.getBody().getMessage()).isNotNull();
    }

    @Test
    public void putLike_whenAuthorizedUserAlreadyLikesThePost_removesTheReactionFromDatabase() {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Post post = postService.save(user, TestUtil.createValidPost());

        String path = String.format(API_1_0_POSTS_LIKE, post.getId());
        testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);

        testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);

        PostReaction reaction = postReactionRepository.findByPostAndUser(post, user);
        assertThat(reaction).isNull();
    }

    @Test
    public void putDislike_whenUnauthorizedUser_returns401() {
        String path = String.format(API_1_0_POSTS_DISLIKE, 5);
        ResponseEntity<Object> result = testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void putDislike_whenAuthorizedUserForUnknownPost_returns404() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticate(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        String path = String.format(API_1_0_POSTS_DISLIKE, 5);
        ResponseEntity<Object> result = testRestTemplate.exchange(RequestEntity.put(new URI(path)).headers(headers).build(), Object.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void putDislike_whenAuthorizedUserForKnownPost_savesPostReactionToDatabase() throws URISyntaxException {
        User inDB = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticate(loggingUser);

        String token = response.getBody().getJwt();

        Post post = postService.save(inDB, TestUtil.createValidPost());

        String path = String.format(API_1_0_POSTS_DISLIKE, post.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        testRestTemplate.exchange(RequestEntity.put(new URI(path)).headers(headers).build(), GenericResponse.class);

        PostReaction reaction = postReactionRepository.findByPostAndUser(post, inDB);
        assertThat(reaction).isNotNull();
    }

    @Test
    public void putDislike_whenAuthorizedUserForKnownPost_returnsSuccessPost() {
        User inDB = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        authenticate(loggingUser);

        Post post = postService.save(inDB, TestUtil.createValidPost());

        String path = String.format(API_1_0_POSTS_DISLIKE, post.getId());
        ResponseEntity<GenericResponse> result = testRestTemplate.exchange(path, HttpMethod.PUT, null, GenericResponse.class);
        assertThat(result.getBody().getMessage()).isNotNull();
    }

    @Test
    public void putDislike_whenAuthorizedUserAlreadyDislikesThePost_removesTheReactionFromDatabase() {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Post post = postService.save(user, TestUtil.createValidPost());

        String path = String.format(API_1_0_POSTS_DISLIKE, post.getId());
        testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);

        testRestTemplate.exchange(path, HttpMethod.PUT, null, Object.class);

        PostReaction reaction = postReactionRepository.findByPostAndUser(post, user);
        assertThat(reaction).isNull();
    }

    @Test
    public void putDislike_whenAuthorizedUserAlreadyLikesThePost_updatesTheReactionInDatabase() {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Post post = postService.save(user, TestUtil.createValidPost());

        String pathLike = String.format(API_1_0_POSTS_LIKE, post.getId());
        testRestTemplate.exchange(pathLike, HttpMethod.PUT, null, Object.class);

        String pathDislike = String.format(API_1_0_POSTS_DISLIKE, post.getId());
        testRestTemplate.exchange(pathDislike, HttpMethod.PUT, null, Object.class);

        PostReaction reaction = postReactionRepository.findByPostAndUser(post, user);
        assertThat(reaction.getReaction()).isEqualTo(Reaction.DISLIKE);
    }

    @Test
    public void putLike_whenAuthorizedUserAlreadyDislikesThePost_updatesTheReactionInDatabase() {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticateUser("user1");

        Post post = postService.save(user, TestUtil.createValidPost());

        String pathDislike = String.format(API_1_0_POSTS_DISLIKE, post.getId());
        testRestTemplate.exchange(pathDislike, HttpMethod.PUT, null, Object.class);

        String pathLike = String.format(API_1_0_POSTS_LIKE, post.getId());
        testRestTemplate.exchange(pathLike, HttpMethod.PUT, null, Object.class);

        PostReaction reaction = postReactionRepository.findByPostAndUser(post, user);
        assertThat(reaction.getReaction()).isEqualTo(Reaction.LIKE);
    }

    private void authenticateUser(String username) {
        testRestTemplate.getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword12@"));
    }

    private ResponseEntity<UserPrincipal> authenticate(LoginRequest loggingUser) {
        ResponseEntity<UserPrincipal> userPrincipalResponseEntity = testRestTemplate.postForEntity("/api/1.0/auth/login", loggingUser, UserPrincipal.class);
        return userPrincipalResponseEntity;
    }

    @AfterEach
    public void cleanupAfter() {
        postReactionRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    @BeforeEach
    public void cleanupBefore() {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }
}
