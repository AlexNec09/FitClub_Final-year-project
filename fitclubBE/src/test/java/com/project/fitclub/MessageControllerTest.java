package com.project.fitclub;

import com.project.fitclub.configuration.AppConfiguration;
import com.project.fitclub.dao.FileAttachmentRepository;
import com.project.fitclub.dao.MessageReactionRepository;
import com.project.fitclub.dao.MessageRepository;
import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.error.ApiError;
import com.project.fitclub.model.FileAttachment;
import com.project.fitclub.model.Message;
import com.project.fitclub.model.Reaction;
import com.project.fitclub.model.User;
import com.project.fitclub.model.vm.MessageVM;
import com.project.fitclub.security.UserPrincipal;
import com.project.fitclub.security.payload.LoginRequest;
import com.project.fitclub.security.payload.MessageRequest;
import com.project.fitclub.service.FileService;
import com.project.fitclub.service.MessageReactionService;
import com.project.fitclub.service.MessageService;
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
public class MessageControllerTest {

    public static final String API_1_0_MESSAGES = "/api/1.0/messages";
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
    FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    FileService fileService;

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    MessageReactionService messageReactionService;

    @Autowired
    MessageReactionRepository messageReactionRepository;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    public void cleanup() throws IOException {
        fileAttachmentRepository.deleteAll();
        messageReactionRepository.deleteAll();
        messageRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @AfterEach
    public void cleanupAfter() {
        fileAttachmentRepository.deleteAll();
        messageReactionRepository.deleteAll();
        messageRepository.deleteAll();
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsAuthorized_receiveOk() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MessageRequest message = TestUtil.createMessageRequest();
        ResponseEntity<Object> result = postMessage(message, headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsUnauthorized_receiveUnauthorized() throws URISyntaxException {
        MessageRequest message = TestUtil.createMessageRequest();
        ResponseEntity<Object> response = postMessage(message, null, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsUnauthorized_receiveApiError() throws URISyntaxException {
        MessageRequest message = TestUtil.createMessageRequest();
        ResponseEntity<ApiError> response = postMessage(message, null, ApiError.class);
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsAuthorized_messageSavedToDatabase() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MessageRequest message = TestUtil.createMessageRequest();
        postMessage(message, headers, Object.class);

        assertThat(messageRepository.count()).isEqualTo(1);
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsAuthorized_messageSavedToDatabaseWithTimestamp() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MessageRequest message = TestUtil.createMessageRequest();

        postMessage(message, headers, Object.class);

        Message inDB = messageRepository.findAll().get(0);

        assertThat(inDB.getTimestamp()).isNotNull();
    }

    @Test
    public void postMessage_whenMessageContentNullAndUserIsAuthorized_receiveBadRequest() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MessageRequest message = new MessageRequest();
        ResponseEntity<Object> result = postMessage(message, headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postMessage_whenMessageContentLessThan10CharactersAndUserIsAuthorized_receiveBadRequest() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MessageRequest message = new MessageRequest();
        message.setContent("1");
        ResponseEntity<Object> result = postMessage(message, headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postMessage_whenMessageContentIs5000CharactersAndUserIsAuthorized_receiveOk() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MessageRequest message = new MessageRequest();
        String veryLongString = IntStream.rangeClosed(1, 5000).mapToObj(i -> "x").collect(Collectors.joining());
        message.setContent(veryLongString);
        ResponseEntity<Object> result = postMessage(message, headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postMessage_whenMessageContentMoreThan5000CharactersAndUserIsAuthorized_receiveBadRequest() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MessageRequest message = new MessageRequest();

        String veryLongString = IntStream.rangeClosed(1, 5001).mapToObj(i -> "x").collect(Collectors.joining());
        message.setContent(veryLongString);
        ResponseEntity<Object> result = postMessage(message, headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postMessage_whenMessageContentNullAndUserIsAuthorized_receiveApiErrorWithValidationErrors() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MessageRequest message = new MessageRequest();

        ResponseEntity<ApiError> result = postMessage(message, headers, ApiError.class);
        Map<String, String> validationErrors = result.getBody().getValidationErrors();

        assertThat(validationErrors.get("content")).isNotNull();
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsAuthorized_messageSavedWithAuthenticatedUserInfo() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MessageRequest message = TestUtil.createMessageRequest();
        postMessage(message, headers, Object.class);

        Message inDB = messageRepository.findAll().get(0);

        assertThat(inDB.getUser().getUsername()).isEqualTo("test-user");
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsAuthorized_messageCanBeAccessedFromUserEntity() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MessageRequest message = TestUtil.createMessageRequest();
        postMessage(message, headers, Object.class);

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        User inDBUser = entityManager.find(User.class, user.getId());
        assertThat(inDBUser.getMessages().size()).isEqualTo(1);
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsAuthorized_receiveMessageVM() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MessageRequest message = TestUtil.createMessageRequest();

        ResponseEntity<MessageVM> result = postMessage(message, headers, MessageVM.class);
        assertThat(result.getBody().getUser().getUsername()).isEqualTo("test-user");
    }

    @Test
    public void postMessage_whenMessageHasFileAttachmentAndUserIsAuthorized_fileAttachmentMessageRelationIsUpdatedInDatabase() throws IOException, URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        MessageRequest message = TestUtil.createMessageRequest();
        message.setAttachment(savedFile);
        ResponseEntity<MessageVM> result = postMessage(message, headers, MessageVM.class);

        FileAttachment inDB = fileAttachmentRepository.findAll().get(0);
        assertThat(inDB.getMessage().getId()).isEqualTo(result.getBody().getId());
    }

    @Test
    public void postMessage_whenMessageHasFileAttachmentAndUserIsAuthorized_fileAttachmentRelationIsUpdatedInDatabase() throws IOException, URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        MessageRequest message = TestUtil.createMessageRequest();
        message.setAttachment(savedFile);
        ResponseEntity<MessageVM> result = postMessage(message, headers, MessageVM.class);

        Message inDB = messageRepository.findById(result.getBody().getId()).get();
        assertThat(inDB.getAttachment().getId()).isEqualTo(savedFile.getId());
    }

    @Test
    public void postMessage_whenMessageHasFileAttachmentAndUserIsAuthorized_receiveMessageVMWithAttachment() throws IOException, URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        MessageRequest message = TestUtil.createMessageRequest();
        message.setAttachment(savedFile);
        ResponseEntity<MessageVM> result = postMessage(message, headers, MessageVM.class);

        assertThat(result.getBody().getAttachment().getName()).isEqualTo(savedFile.getName());
    }

    private MultipartFile createFile() throws IOException {
        ClassPathResource imageResource = new ClassPathResource("test-png.png");
        byte[] fileAsByte = FileUtils.readFileToByteArray(imageResource.getFile());

        return new MockMultipartFile("test-png.png", fileAsByte);
    }

    @Test
    public void getMessages_whenThereAreNoMessages_receiveOk() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Object> responseEntity = getMessages(headers, new ParameterizedTypeReference<>() {
        });
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getMessages_whenThereAreNoMessages_receivePageWithZeroItems() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<TestPage<Object>> result = getMessages(headers, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getMessages_whenThereAreMessages_receivePageWithItems() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<Object>> result = getMessages(headers, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getMessages_whenThereAreMessages_receivePageWithMessageVM() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<MessageVM>> result = getMessages(headers, new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        MessageVM storedMessage = result.getBody().getContent().get(0);
        assertThat(storedMessage.getUser().getUsername()).isEqualTo("test-user");
    }

    @Test
    public void getMessagesOfUser_whenUserExists_receiveOk() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<Object> result = getMessagesOfUser("test-user", headers, new ParameterizedTypeReference<Object>() {
        });
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getMessagesOfUser_whenUserDoesNotExist_receiveUnauthorized() throws URISyntaxException {
        ResponseEntity<Object> response = getMessagesOfUser("unknown-user", null, new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void getMessagesOfUser_whenUserExists_receivePageWithZeroMessages() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<TestPage<Object>> result = getMessagesOfUser("test-user", headers, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getMessagesOfUser_whenUserExistWithMessage_receivePageWithMessageVM() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        messageService.save(myUser, TestUtil.createValidMessage());


        ResponseEntity<TestPage<MessageVM>> result = getMessagesOfUser("test-user", headers, new ParameterizedTypeReference<>() {
        });
        MessageVM storedMessage = result.getBody().getContent().get(0);
        assertThat(storedMessage.getUser().getUsername()).isEqualTo("test-user");
    }

    @Test
    public void getMessagesOfUser_whenUserExistWithMultipleMessages_receivePageWithMatchingMessageCount() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<MessageVM>> result = getMessagesOfUser("test-user", headers, new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getMessagesOfUser_whenMultipleUsersExistWithMultipleMessages_receivePageWithMatchingMessageCount() throws URISyntaxException {
        User userWithThreeMessages = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        IntStream.rangeClosed(1, 3).forEach(i -> {
            messageService.save(userWithThreeMessages, TestUtil.createValidMessage());
        });

        User userWithFiveMessages = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        IntStream.rangeClosed(1, 5).forEach(i -> {
            messageService.save(userWithFiveMessages, TestUtil.createValidMessage());
        });

        ResponseEntity<TestPage<MessageVM>> result = getMessagesOfUser(userWithFiveMessages.getUsername(), headers, new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(5);
    }

    @Test
    public void getMessages_whenLoggedIn_returnsMessagesOfFollowed() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        messageService.save(myUser, TestUtil.createValidMessage());

        Message lastMessage = messageService.save(user2, TestUtil.createValidMessage());
        messageService.save(user3, TestUtil.createValidMessage());
        follow(user2.getId(), headers, Object.class);

        ResponseEntity<TestPage<MessageVM>> result = getMessages(headers, new ParameterizedTypeReference<>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(2);
    }

    @Test
    public void getOldMessages_whenThereAreNoMessagesButUserNotLoggedIn_receiveUnauthorized() throws URISyntaxException {
        ResponseEntity<Object> response = getOldMessages(5, null, new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void getOldMessages_whenThereAreMessagesOfUnfollowedUsers_receivePageWithItemsProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<Object>> result = getOldMessages(fourthMessage.getId(), null, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getOldMessages_whenThereAreMessages_receivePageWithMessageVMBeforeProvidedId() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        messageService.save(myUser, TestUtil.createValidMessage());

        Message lastMessage = messageService.save(user2, TestUtil.createValidMessage());
        messageService.save(user3, TestUtil.createValidMessage());
        follow(user2.getId(), headers, Object.class);

        ResponseEntity<TestPage<MessageVM>> result = getOldMessages(lastMessage.getId(), headers, new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    public void getOldMessagesOfUser_whenUserExistThereAreNoMessages_receiveOk() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<Object> result = getOldMessagesOfUser(5, "test-user", headers, new ParameterizedTypeReference<Object>() {
        });
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getOldMessagesOfUser_whenUserExistAndThereAreMessages_receivePageWithItemsBeforeProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<Object>> result = getOldMessagesOfUser(fourthMessage.getId(), "test-user", headers, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getOldMessagesOfUser_whenUserExistThereAreMessages_receivePageWithMessageVMBeforeProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<MessageVM>> result = getOldMessagesOfUser(fourthMessage.getId(), "test-user", headers, new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        assertThat(result.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getOldMessagesOfUser_whenUserExistThereAreNoMessages_receivePageWithZeroItemsBeforeProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));

        ResponseEntity<TestPage<MessageVM>> result = getOldMessagesOfUser(fourthMessage.getId(), "test-another-user", headers, new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        assertThat(result.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getNewMessages_whenThereAreMessages_receiveListOfItemsAfterProvidedId() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Message firstMessage = messageService.save(myUser, TestUtil.createValidMessage());
        messageService.save(user2, TestUtil.createValidMessage());
        messageService.save(user3, TestUtil.createValidMessage());

        follow(user2.getId(), headers, Object.class);

        ResponseEntity<List<Object>> result = getNewMessages(firstMessage.getId(), headers, new ParameterizedTypeReference<>() {
        });

        assertThat(result.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewMessages_whenThereAreMessages_receiveListOfMessageVMAfterProvidedId() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Message firstMessage = messageService.save(myUser, TestUtil.createValidMessage());
        messageService.save(user2, TestUtil.createValidMessage());
        messageService.save(user3, TestUtil.createValidMessage());

        follow(user2.getId(), headers, Object.class);

        ResponseEntity<List<MessageVM>> result = getNewMessages(firstMessage.getId(), headers, new ParameterizedTypeReference<List<MessageVM>>() {
        });
        assertThat(result.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getNewMessagesOfUser_whenUserExistThereAreNoMessages_receiveOk() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<Object> result = getNewMessagesOfUser(5, "test-user", headers, new ParameterizedTypeReference<Object>() {
        });

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getNewMessagesOfUser_whenUserExistAndThereAreMessages_receiveListWithItemsAfterProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<List<Object>> result = getNewMessagesOfUser(fourthMessage.getId(), "test-user", headers, new ParameterizedTypeReference<List<Object>>() {
        });
        assertThat(result.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewMessagesOfUser_whenUserExistThereAreMessages_receiveListWithMessageVMAfterProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<List<MessageVM>> result = getNewMessagesOfUser(fourthMessage.getId(), "test-user", headers, new ParameterizedTypeReference<List<MessageVM>>() {
        });
        assertThat(result.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getNewMessagesOfUser_whenUserExistThereAreNoMessages_receiveListWithZeroItemsAfterProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));

        ResponseEntity<List<MessageVM>> result = getNewMessagesOfUser(fourthMessage.getId(), "user2", headers, new ParameterizedTypeReference<List<MessageVM>>() {
        });
        assertThat(result.getBody().size()).isEqualTo(0);
    }

    @Test
    public void getNewMessagesCount_whenThereAreMessages_receiveCountAfterProvidedId() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Message firstMessage = messageService.save(myUser, TestUtil.createValidMessage());
        messageService.save(user2, TestUtil.createValidMessage());
        messageService.save(user3, TestUtil.createValidMessage());

        follow(user2.getId(), headers, Object.class);

        ResponseEntity<Map<String, Object>> result = getNewMessagesCount(firstMessage.getId(), headers, new ParameterizedTypeReference<Map<String, Object>>() {
        });

        assertThat(result.getBody().get("count")).isEqualTo(1);
    }

    @Test
    public void getNewMessagesCountOfUser_whenThereAreMessages_receiveCountAfterProvidedId() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<Map<String, Long>> result = getNewMessagesCountOfUser(fourthMessage.getId(), "test-user", headers, new ParameterizedTypeReference<Map<String, Long>>() {
        });
        assertThat(result.getBody().get("count")).isEqualTo(1);
    }

    @Test
    public void getReactions_whenAnonymouslyGetAllWhenThereIsMessageWithReaction_returnsReactionLikeCount() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Message message = messageService.save(myUser, TestUtil.createValidMessage());

        messageReactionService.like(message.getId(), myUser);
        messageReactionService.like(message.getId(), user2);

        messageReactionService.dislike(message.getId(), user3);

        ResponseEntity<TestPage<MessageVM>> result = getMessages(headers, new ParameterizedTypeReference<>() {
        });

        assertThat(result.getBody().getContent().get(0).getReactions().getLikeCount()).isEqualTo(2);
    }

    @Test
    public void getReactions_whenAnonymouslyGetAllWhenThereIsMessageWithReaction_returnsReactionDislikeCount() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Message message = messageService.save(myUser, TestUtil.createValidMessage());

        messageReactionService.like(message.getId(), myUser);
        messageReactionService.like(message.getId(), user2);

        messageReactionService.dislike(message.getId(), user3);

        ResponseEntity<TestPage<MessageVM>> result = getMessages(headers, new ParameterizedTypeReference<>() {
        });

        assertThat(result.getBody().getContent().get(0).getReactions().getDislikeCount()).isEqualTo(1);
    }

    @Test
    public void getReactions_whenThereIsMessageWithReactionWithCurrentLoggedInUser_returnsUsersReaction() throws URISyntaxException {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Message message = messageService.save(myUser, TestUtil.createValidMessage());

        messageReactionService.like(message.getId(), myUser);
        messageReactionService.like(message.getId(), user2);

        messageReactionService.dislike(message.getId(), user3);

        ResponseEntity<TestPage<MessageVM>> result = getMessages(headers, new ParameterizedTypeReference<>() {
        });

        assertThat(result.getBody().getContent().get(0).getReactions().getLoggedUserReaction()).isEqualTo(Reaction.LIKE);

    }

    @Test
    public void getReactions_whenAnonymouslyGetReactionWhenThereIsMessageWithReaction_returnsNullForUserReaction() {
        User myUser = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        User user2 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user2"));
        User user3 = userService.saveWithoutSendingEmail(TestUtil.createValidUser("user3"));
        Message message = messageService.save(myUser, TestUtil.createValidMessage());

        messageReactionService.like(message.getId(), myUser);
        messageReactionService.like(message.getId(), user2);

        messageReactionService.dislike(message.getId(), user3);

        ResponseEntity<TestPage<MessageVM>> result = testRestTemplate.exchange(API_1_0_MESSAGES, HttpMethod.GET, null, new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        assertThat(result.getBody().getContent()).isNull();
    }

    @Test
    public void deleteMessage_whenUserIsUnauthorized_receiveUnauthorized() throws URISyntaxException {
        ResponseEntity<Object> response = deleteMessage(555, null, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void deleteMessage_whenUserIsAuthorized_receiveOk() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        Message message = messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<Object> result = deleteMessage(message.getId(), headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void deleteMessage_whenUserIsAuthorized_receiveGenericResponse() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        Message message = messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<GenericResponse> result = deleteMessage(message.getId(), headers, GenericResponse.class);
        assertThat(result.getBody().getMessage()).isNotNull();
    }

    @Test
    public void deleteMessage_whenUserIsAuthorized_messageRemovedFromDatabase() throws URISyntaxException {
        User user = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        Message message = messageService.save(user, TestUtil.createValidMessage());

        deleteMessage(message.getId(), headers, Object.class);
        Optional<Message> inDB = messageRepository.findById(message.getId());
        assertThat(inDB.isPresent()).isFalse();
    }

    @Test
    public void deleteMessage_whenMessageIsOwnedByAnotherUser_receiveForbidden() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        User messageOwner = userService.saveWithoutSendingEmail(TestUtil.createValidUser("message-owner"));
        Message message = messageService.save(messageOwner, TestUtil.createValidMessage());

        ResponseEntity<Object> result = deleteMessage(message.getId(), headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void deleteMessage_whenMessageDoesNotExist_receiveForbidden() throws URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<Object> result = deleteMessage(555, headers, Object.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void deleteMessage_whenMessageHasAttachment_attachmentRemovedFromDatabase() throws IOException, URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        MessageRequest message = TestUtil.createMessageRequest();
        message.setAttachment(savedFile);
        ResponseEntity<MessageVM> result = postMessage(message, headers, MessageVM.class);

        long messageId = result.getBody().getId();

        deleteMessage(messageId, headers, Object.class);

        Optional<FileAttachment> optionalAttachment = fileAttachmentRepository.findById(savedFile.getId());

        assertThat(optionalAttachment.isPresent()).isFalse();
    }

    @Test
    public void deleteMessage_whenMessageHasAttachment_attachmentRemovedFromStorage() throws IOException, URISyntaxException {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        String token = response.getBody().getJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        MessageRequest message = TestUtil.createMessageRequest();
        message.setAttachment(savedFile);
        ResponseEntity<MessageVM> result = postMessage(message, headers, MessageVM.class);

        long messageId = result.getBody().getId();

        deleteMessage(messageId, headers, Object.class);
        String attachmentFolderPath = appConfiguration.getFullAttachmentsPath() + "/" + savedFile.getName();
        File storedImage = new File(attachmentFolderPath);
        assertThat(storedImage.exists()).isFalse();
    }

    public <T> ResponseEntity<T> deleteMessage(long messageId, HttpHeaders headers, Class<T> responseType) throws URISyntaxException {
        String path = API_1_0_MESSAGES + "/" + messageId;
        return testRestTemplate.exchange(RequestEntity.delete(new URI(path)).headers(headers).build(), responseType);

    }

    public <T> ResponseEntity<T> getNewMessagesCount(long messageId, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = API_1_0_MESSAGES + "/" + messageId + "?direction=after&count=true";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);

    }

    public <T> ResponseEntity<T> follow(long userId, HttpHeaders headers, Class<T> responseType) throws RestClientException, URISyntaxException {
        String path = "/api/1.0/users/" + userId + "/follow";
        return testRestTemplate.exchange(RequestEntity.put(new URI(path)).headers(headers).build(), responseType);

    }

    public <T> ResponseEntity<T> getNewMessagesCountOfUser(long messageId, String username, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = "/api/1.0/users/" + username + "/messages/" + messageId + "?direction=after&count=true";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);
    }

    public <T> ResponseEntity<T> getNewMessages(long messageId, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = API_1_0_MESSAGES + "/" + messageId + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);
    }

    public <T> ResponseEntity<T> getNewMessagesOfUser(long messageId, String username, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = "/api/1.0/users/" + username + "/messages/" + messageId + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);

    }

    public <T> ResponseEntity<T> getOldMessages(long messageId, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = API_1_0_MESSAGES + "/" + messageId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);
    }

    public <T> ResponseEntity<T> getOldMessagesOfUser(long messageId, String username, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = "/api/1.0/users/" + username + "/messages/" + messageId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);
    }

    public <T> ResponseEntity<T> getMessagesOfUser(String username, HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        String path = "/api/1.0/users/" + username + "/messages";
        return testRestTemplate.exchange(RequestEntity.get(new URI(path)).headers(headers).build(), responseType);

    }

    public <T> ResponseEntity<T> getMessages(HttpHeaders headers, ParameterizedTypeReference<T> responseType) throws URISyntaxException {
        return testRestTemplate.exchange(RequestEntity.get(new URI(API_1_0_MESSAGES)).headers(headers).build(), responseType);
    }

    private <T> ResponseEntity<T> postMessage(MessageRequest message, HttpHeaders headers, Class<T> responseType) throws URISyntaxException {
        return testRestTemplate.exchange(RequestEntity.post(new URI(API_1_0_MESSAGES)).headers(headers).body(message), responseType);
    }

    private ResponseEntity<UserPrincipal> authenticateUser(LoginRequest loggingUser) {
        ResponseEntity<UserPrincipal> userPrincipalResponseEntity = testRestTemplate.postForEntity("/api/1.0/auth/login", loggingUser, UserPrincipal.class);
        return userPrincipalResponseEntity;
    }
}
