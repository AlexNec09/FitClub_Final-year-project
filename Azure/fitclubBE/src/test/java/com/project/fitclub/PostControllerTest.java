package com.project.fitclub;

import com.project.fitclub.configuration.AppConfiguration;
import com.project.fitclub.dao.FileAttachmentRepository;
import com.project.fitclub.dao.PostReactionRepository;
import com.project.fitclub.dao.PostRepository;
import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.error.ApiError;
import com.project.fitclub.model.FileAttachment;
import com.project.fitclub.model.Post;
import com.project.fitclub.model.Reaction;
import com.project.fitclub.model.User;
import com.project.fitclub.model.vm.PostVM;
import com.project.fitclub.security.UserPrincipal;
import com.project.fitclub.security.payload.LoginRequest;
import com.project.fitclub.security.payload.PostRequest;
import com.project.fitclub.service.FileService;
import com.project.fitclub.service.PostReactionService;
import com.project.fitclub.service.PostService;
import com.project.fitclub.service.UserService;
import com.project.fitclub.shared.GenericResponse;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
public class PostControllerTest {

    public static final String API_1_0_POSTS = "/api/1.0/posts";
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
    FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    FileService fileService;

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    PostReactionService postReactionService;

    @Autowired
    PostReactionRepository postReactionRepository;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    public void cleanup() throws IOException {
        fileAttachmentRepository.deleteAll();
        postReactionRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @AfterEach
    public void cleanupAfter() {
        fileAttachmentRepository.deleteAll();
        postReactionRepository.deleteAll();
        postRepository.deleteAll();
    }

    @Test
    public void postUserPost_whenPostIsValidAndUserIsAuthorized_receiveOk() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        PostRequest post = TestUtil.createPostRequest();
        ResponseEntity<Object> result = postUserPost(post, headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postUserPost_whenPostIsValidAndUserIsUnauthorized_receiveUnauthorized() throws URISyntaxException {
        PostRequest post = TestUtil.createPostRequest();
        ResponseEntity<Object> response = postUserPost(post, null, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void postUserPost_whenPostIsValidAndUserIsUnauthorized_receiveApiError() throws URISyntaxException {
        PostRequest post = TestUtil.createPostRequest();
        ResponseEntity<ApiError> response = postUserPost(post, null, ApiError.class);
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void postUserPost_whenPostIsValidAndUserIsAuthorized_postSavedToDatabase() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        PostRequest post = TestUtil.createPostRequest();
        postUserPost(post, headers, Object.class);

        assertThat(postRepository.count()).isEqualTo(1);
    }

    @Test
    public void postUserPost_whenPostIsValidAndUserIsAuthorized_postSavedToDatabaseWithTimestamp() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        PostRequest post = TestUtil.createPostRequest();

        postUserPost(post, headers, Object.class);

        Post inDB = postRepository.findAll().get(0);

        assertThat(inDB.getTimestamp()).isNotNull();
    }

    @Test
    public void postUserPost_whenPostContentNullAndUserIsAuthorized_receiveBadRequest() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        PostRequest post = new PostRequest();
        ResponseEntity<Object> result = postUserPost(post, headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserPost_whenPostContentLessThan10CharactersAndUserIsAuthorized_receiveBadRequest() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        PostRequest post = new PostRequest();
        post.setContent("1");
        ResponseEntity<Object> result = postUserPost(post, headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserPost_whenPostContentIs5000CharactersAndUserIsAuthorized_receiveOk() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        PostRequest post = new PostRequest();
        String veryLongString = IntStream.rangeClosed(1, 5000).mapToObj(i -> "x").collect(Collectors.joining());
        post.setContent(veryLongString);
        ResponseEntity<Object> result = postUserPost(post, headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postUserPost_whenPostContentMoreThan5000CharactersAndUserIsAuthorized_receiveBadRequest() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        PostRequest post = new PostRequest();

        String veryLongString = IntStream.rangeClosed(1, 5001).mapToObj(i -> "x").collect(Collectors.joining());
        post.setContent(veryLongString);
        ResponseEntity<Object> result = postUserPost(post, headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserPost_whenPostContentNullAndUserIsAuthorized_receiveApiErrorWithValidationErrors() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        PostRequest post = new PostRequest();

        ResponseEntity<ApiError> result = postUserPost(post, headers, ApiError.class);
        Map<String, String> validationErrors = result.getBody().getValidationErrors();

        assertThat(validationErrors.get("content")).isNotNull();
    }

    @Test
    public void postUserPost_whenPostIsValidAndUserIsAuthorized_postSavedWithAuthenticatedUserInfo() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        PostRequest post = TestUtil.createPostRequest();
        postUserPost(post, headers, Object.class);

        Post inDB = postRepository.findAll().get(0);

        assertThat(inDB.getUser().getUsername()).isEqualTo("test-user");
    }

    @Test
    public void postUserPost_whenPostIsValidAndUserIsAuthorized_postCanBeAccessedFromUserEntity() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        PostRequest post = TestUtil.createPostRequest();
        postUserPost(post, headers, Object.class);

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        User inDBUser = entityManager.find(User.class, user.getId());
        assertThat(inDBUser.getPosts().size()).isEqualTo(1);
    }

    @Test
    public void postUserPost_whenPostIsValidAndUserIsAuthorized_receivePostVM() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        PostRequest post = TestUtil.createPostRequest();

        ResponseEntity<PostVM> result = postUserPost(post, headers, PostVM.class);
        assertThat(result.getBody().getUser().getUsername()).isEqualTo("test-user");
    }

    @Test
    public void postUserPost_whenPostHasFileAttachmentAndUserIsAuthorized_fileAttachmentPostRelationIsUpdatedInDatabase() throws IOException, URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        PostRequest post = TestUtil.createPostRequest();
        post.setAttachment(savedFile);
        ResponseEntity<PostVM> result = postUserPost(post, headers, PostVM.class);

        FileAttachment inDB = fileAttachmentRepository.findAll().get(0);
        assertThat(inDB.getPost().getId()).isEqualTo(result.getBody().getId());
    }

    @Test
    public void postUserPost_whenPostHasFileAttachmentAndUserIsAuthorized_fileAttachmentRelationIsUpdatedInDatabase() throws IOException, URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        PostRequest post = TestUtil.createPostRequest();
        post.setAttachment(savedFile);
        ResponseEntity<PostVM> result = postUserPost(post, headers, PostVM.class);

        Post inDB = postRepository.findById(result.getBody().getId()).get();
        assertThat(inDB.getAttachment().getId()).isEqualTo(savedFile.getId());
    }

    @Test
    public void postUserPost_whenPostHasFileAttachmentAndUserIsAuthorized_receivePostVMWithAttachment() throws IOException, URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        PostRequest post = TestUtil.createPostRequest();
        post.setAttachment(savedFile);
        ResponseEntity<PostVM> result = postUserPost(post, headers, PostVM.class);

        assertThat(result.getBody().getAttachment().getName()).isEqualTo(savedFile.getName());
    }

    private MultipartFile createFile() throws IOException {
        ClassPathResource imageResource = new ClassPathResource("test-png.png");
        byte[] fileAsByte = FileUtils.readFileToByteArray(imageResource.getFile());

        return new MockMultipartFile("test-png.png", fileAsByte);
    }

    @Test
    public void getPosts_whenThereAreNoPosts_receiveOk() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Object> responseEntity = getPosts(headers, new ParameterizedTypeReference<Object>() {
        });
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getPosts_whenThereAreNoPosts_receivePageWithZeroItems() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<TestPage<Object>> result = getPosts(headers, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getPosts_whenThereArePosts_receivePageWithItems() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());

        ResponseEntity<TestPage<Object>> result = getPosts(headers, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getPosts_whenThereArePosts_receivePageWithPostVM() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        postService.save(user, TestUtil.createValidPost());

        ResponseEntity<TestPage<PostVM>> result = getPosts(headers, new ParameterizedTypeReference<TestPage<PostVM>>() {
        });
        PostVM storedPost = result.getBody().getContent().get(0);
        assertThat(storedPost.getUser().getUsername()).isEqualTo("test-user");
    }

    @Test
    public void getPostsOfUser_whenUserExists_receiveOk() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<Object> result = getPostsOfUser("test-user", headers, new ParameterizedTypeReference<Object>() {
        });
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getPostsOfUser_whenUserDoesNotExist_receiveUnauthorized() throws URISyntaxException {
        ResponseEntity<Object> response = getPostsOfUser("unknown-user", null, new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void getPostsOfUser_whenUserExists_receivePageWithZeroPosts() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<TestPage<Object>> result = getPostsOfUser("test-user", headers, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getPostsOfUser_whenUserExistWithPost_receivePageWithPostVM() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        postService.save(myUser, TestUtil.createValidPost());


        ResponseEntity<TestPage<PostVM>> result = getPostsOfUser("test-user", headers, new ParameterizedTypeReference<TestPage<PostVM>>() {
        });
        PostVM storedPost = result.getBody().getContent().get(0);
        assertThat(storedPost.getUser().getUsername()).isEqualTo("test-user");
    }

    @Test
    public void getPostsOfUser_whenUserExistWithMultiplePosts_receivePageWithMatchingPostCount() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());

        ResponseEntity<TestPage<PostVM>> result = getPostsOfUser("test-user", headers, new ParameterizedTypeReference<TestPage<PostVM>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getPostsOfUser_whenMultipleUsersExistWithMultiplePosts_receivePageWithMatchingPostCount() throws URISyntaxException {
        User userWithThreePosts = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        IntStream.rangeClosed(1, 3).forEach(i -> {
            postService.save(userWithThreePosts, TestUtil.createValidPost());
        });

        User userWithFivePosts = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        IntStream.rangeClosed(1, 5).forEach(i -> {
            postService.save(userWithFivePosts, TestUtil.createValidPost());
        });

        ResponseEntity<TestPage<PostVM>> result = getPostsOfUser(userWithFivePosts.getUsername(), headers, new ParameterizedTypeReference<TestPage<PostVM>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(5);
    }

    @Test
    public void getPosts_whenLoggedIn_returnsPostsOfFollowedUsers() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        postService.save(myUser, TestUtil.createValidPost());
        postService.save(user2, TestUtil.createValidPost());
        postService.save(user3, TestUtil.createValidPost());
        follow(user2.getId(), headers, Object.class);

        ResponseEntity<TestPage<PostVM>> result = getPosts(headers, new ParameterizedTypeReference<TestPage<PostVM>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(2);
    }

    @Test
    public void getOldPosts_whenThereAreNoPostsButUserNotLoggedIn_receiveUnauthorized() throws URISyntaxException {
        ResponseEntity<Object> response = getOldPosts(5, null, new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void getOldPosts_whenThereArePostsOfUnfollowedUsers_receivePageWithItemsProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        Post fourthPost = postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());

        ResponseEntity<TestPage<Object>> result = getOldPosts(fourthPost.getId(), null, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getOldPosts_whenThereArePosts_receivePageWithPostVMBeforeProvidedId() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        postService.save(myUser, TestUtil.createValidPost());

        Post lastPost = postService.save(user2, TestUtil.createValidPost());
        postService.save(user3, TestUtil.createValidPost());
        follow(user2.getId(), headers, Object.class);

        ResponseEntity<TestPage<PostVM>> result = getOldPosts(lastPost.getId(), headers, new ParameterizedTypeReference<TestPage<PostVM>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    public void getOldPostsOfUser_whenUserExistThereAreNoPosts_receiveOk() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<Object> result = getOldPostsOfUser(5, "test-user", headers, new ParameterizedTypeReference<Object>() {
        });
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getOldPostsOfUser_whenUserExistAndThereArePosts_receivePageWithItemsBeforeProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        Post fourthPost = postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());

        ResponseEntity<TestPage<Object>> result = getOldPostsOfUser(fourthPost.getId(), "test-user", headers, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getOldPostsOfUser_whenUserExistThereArePosts_receivePageWithPostVMBeforeProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        Post fourthPost = postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());

        ResponseEntity<TestPage<PostVM>> result = getOldPostsOfUser(fourthPost.getId(), "test-user", headers, new ParameterizedTypeReference<TestPage<PostVM>>() {
        });
        assertThat(result.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getOldPostsOfUser_whenUserExistThereAreNoPosts_receivePageWithZeroItemsBeforeProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        Post fourthPost = postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());

        userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));

        ResponseEntity<TestPage<PostVM>> result = getOldPostsOfUser(fourthPost.getId(), "test-another-user", headers, new ParameterizedTypeReference<TestPage<PostVM>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getNewPosts_whenThereArePosts_receiveListOfItemsAfterProvidedId() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Post firstPost = postService.save(myUser, TestUtil.createValidPost());
        postService.save(user2, TestUtil.createValidPost());
        postService.save(user3, TestUtil.createValidPost());

        follow(user2.getId(), headers, Object.class);

        ResponseEntity<List<Object>> result = getNewPosts(firstPost.getId(), headers, new ParameterizedTypeReference<List<Object>>() {
        });

        assertThat(result.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewPosts_whenThereArePosts_receiveListOfPostVMAfterProvidedId() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Post firstPost = postService.save(myUser, TestUtil.createValidPost());
        postService.save(user2, TestUtil.createValidPost());
        postService.save(user3, TestUtil.createValidPost());

        follow(user2.getId(), headers, Object.class);

        ResponseEntity<List<PostVM>> result = getNewPosts(firstPost.getId(), headers, new ParameterizedTypeReference<List<PostVM>>() {
        });
        assertThat(result.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getNewPostsOfUser_whenUserExistThereAreNoPosts_receiveOk() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<Object> result = getNewPostsOfUser(5, "test-user", headers, new ParameterizedTypeReference<Object>() {
        });

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getNewPostsOfUser_whenUserExistAndThereArePosts_receiveListWithItemsAfterProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        Post fourthPost = postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());

        ResponseEntity<List<Object>> result = getNewPostsOfUser(fourthPost.getId(), "test-user", headers, new ParameterizedTypeReference<List<Object>>() {
        });
        assertThat(result.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewPostsOfUser_whenUserExistThereArePosts_receiveListWithPostVMAfterProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        Post fourthPost = postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());

        ResponseEntity<List<PostVM>> result = getNewPostsOfUser(fourthPost.getId(), "test-user", headers, new ParameterizedTypeReference<List<PostVM>>() {
        });
        assertThat(result.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getNewPostsOfUser_whenUserExistThereAreNoPosts_receiveListWithZeroItemsAfterProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        Post fourthPost = postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());

        userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));

        ResponseEntity<List<PostVM>> result = getNewPostsOfUser(fourthPost.getId(), "user2", headers, new ParameterizedTypeReference<List<PostVM>>() {
        });
        assertThat(result.getBody().size()).isEqualTo(0);
    }

    @Test
    public void getNewPostsCount_whenThereArePosts_receiveCountAfterProvidedId() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Post firstPost = postService.save(myUser, TestUtil.createValidPost());
        postService.save(user2, TestUtil.createValidPost());
        postService.save(user3, TestUtil.createValidPost());

        follow(user2.getId(), headers, Object.class);

        ResponseEntity<Map<String, Object>> result = getNewPostsCount(firstPost.getId(), headers, new ParameterizedTypeReference<Map<String, Object>>() {
        });

        assertThat(result.getBody().get("count")).isEqualTo(1);
    }

    @Test
    public void getNewPostsCountOfUser_whenThereArePosts_receiveCountAfterProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());
        Post fourthPost = postService.save(user, TestUtil.createValidPost());
        postService.save(user, TestUtil.createValidPost());

        ResponseEntity<Map<String, Long>> result = getNewPostsCountOfUser(fourthPost.getId(), "test-user", headers, new ParameterizedTypeReference<Map<String, Long>>() {
        });
        assertThat(result.getBody().get("count")).isEqualTo(1);
    }

    @Test
    public void getReactions_whenAnonymouslyGetAllWhenThereIsPostWithReaction_returnsReactionLikeCount() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Post post = postService.save(myUser, TestUtil.createValidPost());

        postReactionService.like(post.getId(), myUser);
        postReactionService.like(post.getId(), user2);

        postReactionService.dislike(post.getId(), user3);

        ResponseEntity<TestPage<PostVM>> result = getPosts(headers, new ParameterizedTypeReference<TestPage<PostVM>>() {
        });

        assertThat(result.getBody().getContent().get(0).getReactions().getLikeCount()).isEqualTo(2);
    }

    @Test
    public void getReactions_whenAnonymouslyGetAllWhenThereIsPostWithReaction_returnsReactionDislikeCount() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Post post = postService.save(myUser, TestUtil.createValidPost());

        postReactionService.like(post.getId(), myUser);
        postReactionService.like(post.getId(), user2);

        postReactionService.dislike(post.getId(), user3);

        ResponseEntity<TestPage<PostVM>> result = getPosts(headers, new ParameterizedTypeReference<TestPage<PostVM>>() {
        });

        assertThat(result.getBody().getContent().get(0).getReactions().getDislikeCount()).isEqualTo(1);
    }

    @Test
    public void getReactions_whenThereIsPostWithReactionWithCurrentLoggedInUser_returnsUsersReaction() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Post post = postService.save(myUser, TestUtil.createValidPost());

        postReactionService.like(post.getId(), myUser);
        postReactionService.like(post.getId(), user2);

        postReactionService.dislike(post.getId(), user3);

        ResponseEntity<TestPage<PostVM>> result = getPosts(headers, new ParameterizedTypeReference<TestPage<PostVM>>() {
        });

        assertThat(result.getBody().getContent().get(0).getReactions().getLoggedUserReaction()).isEqualTo(Reaction.LIKE);

    }

    @Test
    public void getReactions_whenAnonymouslyGetReactionWhenThereIsPostWithReaction_returnsNullForUserReaction() {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Post post = postService.save(myUser, TestUtil.createValidPost());

        postReactionService.like(post.getId(), myUser);
        postReactionService.like(post.getId(), user2);

        postReactionService.dislike(post.getId(), user3);

        ResponseEntity<TestPage<PostVM>> result = testRestTemplate.exchange(API_1_0_POSTS, HttpMethod.GET, null, new ParameterizedTypeReference<TestPage<PostVM>>() {
        });
        assertThat(result.getBody().getContent()).isNull();
    }

    @Test
    public void deletePost_whenUserIsUnauthorized_receiveUnauthorized() throws URISyntaxException {
        ResponseEntity<Object> response = deletePost(555, null, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void deletePost_whenUserIsAuthorized_receiveOk() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        Post post = postService.save(user, TestUtil.createValidPost());

        ResponseEntity<Object> result = deletePost(post.getId(), headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void deletePost_whenUserIsAuthorized_receiveGenericResponse() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        Post post = postService.save(user, TestUtil.createValidPost());

        ResponseEntity<GenericResponse> result = deletePost(post.getId(), headers, GenericResponse.class);
        assertThat(result.getBody().getMessage()).isNotNull();
    }

    @Test
    public void deletePost_whenUserIsAuthorized_postRemovedFromDatabase() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        Post post = postService.save(user, TestUtil.createValidPost());

        deletePost(post.getId(), headers, Object.class);
        Optional<Post> inDB = postRepository.findById(post.getId());
        assertThat(inDB.isPresent()).isFalse();
    }

    @Test
    public void deletePost_whenPostIsOwnedByAnotherUser_receiveForbidden() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User postOwner = userService.saveWithoutSendingEmail(TestUtil.createValidUser("post-owner"));
        Post post = postService.save(postOwner, TestUtil.createValidPost());

        ResponseEntity<Object> result = deletePost(post.getId(), headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void deletePost_whenPostDoesNotExist_receiveForbidden() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<Object> result = deletePost(555, headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void deletePost_whenPostHasAttachment_attachmentRemovedFromDatabase() throws IOException, URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        PostRequest post = TestUtil.createPostRequest();
        post.setAttachment(savedFile);
        ResponseEntity<PostVM> result = postUserPost(post, headers, PostVM.class);

        long postId = result.getBody().getId();

        deletePost(postId, headers, Object.class);

        Optional<FileAttachment> optionalAttachment = fileAttachmentRepository.findById(savedFile.getId());

        assertThat(optionalAttachment.isPresent()).isFalse();
    }

    @Test
    public void deletePost_whenPostHasAttachment_attachmentRemovedFromStorage() throws IOException, URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        PostRequest post = TestUtil.createPostRequest();
        post.setAttachment(savedFile);
        ResponseEntity<PostVM> result = postUserPost(post, headers, PostVM.class);

        long postId = result.getBody().getId();

        deletePost(postId, headers, Object.class);
        String attachmentFolderPath = appConfiguration.getFullAttachmentsPath() + "/" + savedFile.getName();
        File storedImage = new File(attachmentFolderPath);
        assertThat(storedImage.exists()).isFalse();
    }

    public <T> ResponseEntity<T> deletePost(long postId, HttpHeaders headers, Class<T> responseType) throws URISyntaxException {
        String path = API_1_0_POSTS + "/" + postId;
        return testRestTemplate.exchange(RequestEntity.delete(new URI(path)).headers(headers).build(), responseType);

    }

    public <T> ResponseEntity<T> getNewPostsCount(long postId, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = API_1_0_POSTS + "/" + postId + "?direction=after&count=true";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);

    }

    public <T> ResponseEntity<T> follow(long userId, HttpHeaders headers, Class<T> responseType) throws RestClientException, URISyntaxException {
        String path = "/api/1.0/users/" + userId + "/follow";
        return testRestTemplate.exchange(RequestEntity.put(new URI(path)).headers(headers).build(), responseType);

    }

    public <T> ResponseEntity<T> getNewPostsCountOfUser(long postId, String username, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = "/api/1.0/users/" + username + "/posts/" + postId + "?direction=after&count=true";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);
    }

    public <T> ResponseEntity<T> getNewPosts(long postId, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = API_1_0_POSTS + "/" + postId + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);
    }

    public <T> ResponseEntity<T> getNewPostsOfUser(long postId, String username, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = "/api/1.0/users/" + username + "/posts/" + postId + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);

    }

    public <T> ResponseEntity<T> getOldPosts(long postId, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = API_1_0_POSTS + "/" + postId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);
    }

    public <T> ResponseEntity<T> getOldPostsOfUser(long postId, String username, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = "/api/1.0/users/" + username + "/posts/" + postId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);
    }

    public <T> ResponseEntity<T> getPostsOfUser(String username, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = "/api/1.0/users/" + username + "/posts";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);

    }

    public <T> ResponseEntity<T> getPosts(HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        return testRestTemplate.exchange(RequestEntity.get(new URI(API_1_0_POSTS)).headers(headers).build(), responseType);
    }

    private <T> ResponseEntity<T> postUserPost(PostRequest post, HttpHeaders headers, Class<T> responseType) throws URISyntaxException {
        return testRestTemplate.exchange(RequestEntity.post(new URI(API_1_0_POSTS)).headers(headers).body(post), responseType);
    }

    private ResponseEntity<UserPrincipal> authenticateUser(LoginRequest loggingUser) {
        ResponseEntity<UserPrincipal> userPrincipalResponseEntity = testRestTemplate.postForEntity("/api/1.0/auth/login", loggingUser, UserPrincipal.class);
        return userPrincipalResponseEntity;
    }
}
