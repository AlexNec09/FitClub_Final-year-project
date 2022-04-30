package com.fitclub.fitclub;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitclub.fitclub.configuration.AppConfiguration;
import com.fitclub.fitclub.error.ApiError;
import com.fitclub.fitclub.model.Entity.FileAttachment;
import com.fitclub.fitclub.dao.attachment.FileAttachmentRepository;
import com.fitclub.fitclub.service.FileService;
import com.fitclub.fitclub.model.Entity.Message;
import com.fitclub.fitclub.dao.message.MessageRepository;
import com.fitclub.fitclub.service.MessageService;
import com.fitclub.fitclub.model.MessageVM;
import com.fitclub.fitclub.shared.GenericResponse;
import com.fitclub.fitclub.model.Entity.User;
import com.fitclub.fitclub.dao.user.UserRepository;
import com.fitclub.fitclub.service.UserService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
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

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    public void cleanup() throws IOException {
        fileAttachmentRepository.deleteAll();
        messageRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @AfterEach
    public void cleanupAfter() {
        fileAttachmentRepository.deleteAll();
        messageRepository.deleteAll();
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsAuthorized_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = TestUtil.createValidMessage();
        ResponseEntity<Object> response = postMessage(message, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsUnauthorized_receiveUnauthorized() {
        Message message = TestUtil.createValidMessage();
        ResponseEntity<Object> response = postMessage(message, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsUnauthorized_receiveApiError() {
        Message message = TestUtil.createValidMessage();
        ResponseEntity<ApiError> response = postMessage(message, ApiError.class);
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsAuthorized_messageSavedToDatabase() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = TestUtil.createValidMessage();
        postMessage(message, Object.class);

        assertThat(messageRepository.count()).isEqualTo(1);
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsAuthorized_messageSavedToDatabaseWithTimestamp() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = TestUtil.createValidMessage();
        postMessage(message, Object.class);

        Message inDB = messageRepository.findAll().get(0);

        assertThat(inDB.getTimestamp()).isNotNull();
    }

    @Test
    public void postMessage_whenMessageContentNullAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = new Message();
        ResponseEntity<Object> response = postMessage(message, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postMessage_whenMessageContentLessThan10CharactersAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = new Message();
        message.setContent("123456789");
        ResponseEntity<Object> response = postMessage(message, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postMessage_whenMessageContentIs5000CharactersAndUserIsAuthorized_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = new Message();
        String veryLongString = IntStream.rangeClosed(1, 5000).mapToObj(i -> "x").collect(Collectors.joining());
        message.setContent(veryLongString);
        ResponseEntity<Object> response = postMessage(message, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postMessage_whenMessageContentMoreThan5000CharactersAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = new Message();
        String veryLongString = IntStream.rangeClosed(1, 5001).mapToObj(i -> "x").collect(Collectors.joining());
        message.setContent(veryLongString);
        ResponseEntity<Object> response = postMessage(message, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postMessage_whenMessageContentNullAndUserIsAuthorized_receiveApiErrorWithValidationErrors() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = new Message();
        ResponseEntity<ApiError> response = postMessage(message, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();

        assertThat(validationErrors.get("content")).isNotNull();
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsAuthorized_messageSavedWithAuthenticatedUserInfo() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = TestUtil.createValidMessage();
        postMessage(message, Object.class);

        Message inDB = messageRepository.findAll().get(0);

        assertThat(inDB.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsAuthorized_messageCanBeAccessedFromUserEntity() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = TestUtil.createValidMessage();
        postMessage(message, Object.class);

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        User inDBUser = entityManager.find(User.class, user.getId());
        assertThat(inDBUser.getMessages().size()).isEqualTo(1);
    }

    @Test
    public void postMessage_whenMessageIsValidAndUserIsAuthorized_receiveMessageVM() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = TestUtil.createValidMessage();
        ResponseEntity<MessageVM> response = postMessage(message, MessageVM.class);
        assertThat(response.getBody().getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void postMessage_whenMessageHasFileAttachmentAndUserIsAuthorized_fileAttachmentMessageRelationIsUpdatedInDatabase() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Message message = TestUtil.createValidMessage();
        message.setAttachment(savedFile);
        ResponseEntity<MessageVM> response = postMessage(message, MessageVM.class);

        FileAttachment inDB = fileAttachmentRepository.findAll().get(0);
        assertThat(inDB.getMessage().getId()).isEqualTo(response.getBody().getId());
    }

    @Test
    public void postMessage_whenMessageHasFileAttachmentAndUserIsAuthorized_fileAttachmentRelationIsUpdatedInDatabase() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Message message = TestUtil.createValidMessage();
        message.setAttachment(savedFile);
        ResponseEntity<MessageVM> response = postMessage(message, MessageVM.class);

        Message inDB = messageRepository.findById(response.getBody().getId()).get();
        assertThat(inDB.getAttachment().getId()).isEqualTo(savedFile.getId());
    }

    @Test
    public void postMessage_whenMessageHasFileAttachmentAndUserIsAuthorized_receiveMessageVMWithAttachment() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Message message = TestUtil.createValidMessage();
        message.setAttachment(savedFile);
        ResponseEntity<MessageVM> response = postMessage(message, MessageVM.class);

        assertThat(response.getBody().getAttachment().getName()).isEqualTo(savedFile.getName());
    }

    private MultipartFile createFile() throws IOException {
        ClassPathResource imageResource = new ClassPathResource("test-png.png");
        byte[] fileAsByte = FileUtils.readFileToByteArray(imageResource.getFile());

        MultipartFile file = new MockMultipartFile("test-png.png", fileAsByte);
        return file;
    }

    @Test
    public void getMessages_whenThereAreNoMessages_receiveOk() {
        ResponseEntity<Object> responseEntity = getMessages(new ParameterizedTypeReference<Object>() {
        });
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getMessages_whenThereAreNoMessages_receivePageWithZeroItems() {
        ResponseEntity<TestPage<Object>> response = getMessages(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getMessages_whenThereAreMessages_receivePageWithItems() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<Object>> response = getMessages(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getMessages_whenThereAreMessages_receivePageWithMessageVM() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<MessageVM>> response = getMessages(new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        MessageVM storedMessage = response.getBody().getContent().get(0);
        assertThat(storedMessage.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void getMessagesOfUser_whenUserExists_receiveOk() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getMessagesOfUser("user1", new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getMessagesOfUser_whenUserDoesNotExist_receiveNotFound() {
        ResponseEntity<Object> response = getMessagesOfUser("unknown-user", new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getMessagesOfUser_whenUserExists_receivePageWithZeroMessages() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<TestPage<Object>> response = getMessagesOfUser("user1", new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getMessagesOfUser_whenUserExistWithMessage_receivePageWithMessageVM() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<MessageVM>> response = getMessagesOfUser("user1", new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        MessageVM storedMessage = response.getBody().getContent().get(0);
        assertThat(storedMessage.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void getMessagesOfUser_whenUserExistWithMultipleMessages_receivePageWithMatchingMessageCount() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<MessageVM>> response = getMessagesOfUser("user1", new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getMessagesOfUser_whenMultipleUsersExistWithMultipleMessages_receivePageWithMatchingMessageCount() {
        User userWithThreeMessages = userService.save(TestUtil.createValidUser("user1"));
        IntStream.rangeClosed(1, 3).forEach(i -> {
            messageService.save(userWithThreeMessages, TestUtil.createValidMessage());
        });

        User userWithFiveMessages = userService.save(TestUtil.createValidUser("user2"));
        IntStream.rangeClosed(1, 5).forEach(i -> {
            messageService.save(userWithFiveMessages, TestUtil.createValidMessage());
        });

        ResponseEntity<TestPage<MessageVM>> response = getMessagesOfUser(userWithFiveMessages.getUsername(), new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        assertThat(response.getBody().getTotalElements()).isEqualTo(5);
    }

    @Test
    public void getOldMessages_whenThereAreNoMessages_receiveOk() {
        ResponseEntity<Object> response = getOldMessages(5, new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getOldMessages_whenThereAreMessages_receivePageWithItemsProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<Object>> response = getOldMessages(fourthMessage.getId(), new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getOldMessages_whenThereAreMessages_receivePageWithMessageVMBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<MessageVM>> response = getOldMessages(fourthMessage.getId(), new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        assertThat(response.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getOldMessagesOfUser_whenUserExistThereAreNoMessages_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getOldMessagesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getOldMessagesOfUser_whenUserExistAndThereAreMessages_receivePageWithItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<Object>> response = getOldMessagesOfUser(fourthMessage.getId(), "user1", new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getOldMessagesOfUser_whenUserExistThereAreMessages_receivePageWithMessageVMBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<TestPage<MessageVM>> response = getOldMessagesOfUser(fourthMessage.getId(), "user1", new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        assertThat(response.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getOldMessagesOfUser_whenUserDoesNotExistThereAreNoMessages_receiveNotFound() {
        ResponseEntity<Object> response = getOldMessagesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getOldMessagesOfUser_whenUserExistThereAreNoMessages_receivePageWithZeroItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        userService.save(TestUtil.createValidUser("user2"));

        ResponseEntity<TestPage<MessageVM>> response = getOldMessagesOfUser(fourthMessage.getId(), "user2", new ParameterizedTypeReference<TestPage<MessageVM>>() {
        });
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getNewMessages_whenThereAreMessages_receiveListOfItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<List<Object>> response = getNewMessages(fourthMessage.getId(), new ParameterizedTypeReference<List<Object>>() {
        });
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewMessages_whenThereAreMessages_receiveListOfMessageVMAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<List<MessageVM>> response = getNewMessages(fourthMessage.getId(), new ParameterizedTypeReference<List<MessageVM>>() {
        });
        assertThat(response.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getNewMessagesOfUser_whenUserExistThereAreNoMessages_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getNewMessagesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getNewMessagesOfUser_whenUserExistAndThereAreMessages_receiveListWithItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<List<Object>> response = getNewMessagesOfUser(fourthMessage.getId(), "user1", new ParameterizedTypeReference<List<Object>>() {
        });
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewMessagesOfUser_whenUserExistThereAreMessages_receiveListWithMessageVMAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<List<MessageVM>> response = getNewMessagesOfUser(fourthMessage.getId(), "user1", new ParameterizedTypeReference<List<MessageVM>>() {
        });
        assertThat(response.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getNewMessagesOfUser_whenUserDoesNotExistThereAreNoMessages_receiveNotFound() {
        ResponseEntity<Object> response = getNewMessagesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getNewMessagesOfUser_whenUserExistThereAreNoMessages_receiveListWithZeroItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        userService.save(TestUtil.createValidUser("user2"));

        ResponseEntity<List<MessageVM>> response = getNewMessagesOfUser(fourthMessage.getId(), "user2", new ParameterizedTypeReference<List<MessageVM>>() {
        });
        assertThat(response.getBody().size()).isEqualTo(0);
    }

    @Test
    public void getNewMessagesCount_whenThereAreMessages_receiveCountAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<Map<String, Long>> response = getNewMessagesCount(fourthMessage.getId(), new ParameterizedTypeReference<Map<String, Long>>() {
        });
        assertThat(response.getBody().get("count")).isEqualTo(1);
    }

    @Test
    public void getNewMessagesCountOfUser_whenThereAreMessages_receiveCountAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());
        Message fourthMessage = messageService.save(user, TestUtil.createValidMessage());
        messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<Map<String, Long>> response = getNewMessagesCountOfUser(fourthMessage.getId(), "user1", new ParameterizedTypeReference<Map<String, Long>>() {
        });
        assertThat(response.getBody().get("count")).isEqualTo(1);
    }

    @Test
    public void deleteMessage_whenUserIsUnauthorized_receiveUnauthorized() {
        ResponseEntity<Object> response = deleteMessage(555, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void deleteMessage_whenUserIsAuthorized_receiveOk() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<Object> response = deleteMessage(message.getId(), Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void deleteMessage_whenUserIsAuthorized_receiveGenericResponse() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = messageService.save(user, TestUtil.createValidMessage());

        ResponseEntity<GenericResponse> response = deleteMessage(message.getId(), GenericResponse.class);
        assertThat(response.getBody().getMessage()).isNotNull();
    }

    @Test
    public void deleteMessage_whenUserIsAuthorized_messageRemovedFromDatabase() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Message message = messageService.save(user, TestUtil.createValidMessage());

        deleteMessage(message.getId(), Object.class);
        Optional<Message> inDB = messageRepository.findById(message.getId());
        assertThat(inDB.isPresent()).isFalse();
    }

    @Test
    public void deleteMessage_whenMessageIsOwnedByAnotherUser_receiveForbidden() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        User messageOwner = userService.save(TestUtil.createValidUser("message-owner"));
        Message message = messageService.save(messageOwner, TestUtil.createValidMessage());

        ResponseEntity<Object> response = deleteMessage(message.getId(), Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void deleteMessage_whenMessageDoesNotExist_receiveForbidden() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        ResponseEntity<Object> response = deleteMessage(555, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void deleteMessage_whenMessageHasAttachment_attachmentRemovedFromDatabase() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Message message = TestUtil.createValidMessage();
        message.setAttachment(savedFile);
        ResponseEntity<MessageVM> response = postMessage(message, MessageVM.class);

        long messageId = response.getBody().getId();

        deleteMessage(messageId, Object.class);

        Optional<FileAttachment> optionalAttachment = fileAttachmentRepository.findById(savedFile.getId());

        assertThat(optionalAttachment.isPresent()).isFalse();
    }

    @Test
    public void deleteMessage_whenMessageHasAttachment_attachmentRemovedFromStorage() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Message message = TestUtil.createValidMessage();
        message.setAttachment(savedFile);
        ResponseEntity<MessageVM> response = postMessage(message, MessageVM.class);

        long messageId = response.getBody().getId();

        deleteMessage(messageId, Object.class);
        String attachmentFolderPath = appConfiguration.getFullAttachmentsPath() + "/" + savedFile.getName();
        File storedImage = new File(attachmentFolderPath);
        assertThat(storedImage.exists()).isFalse();
    }

    public <T> ResponseEntity<T> deleteMessage(long messageId, Class<T> responseType) {
        return testRestTemplate.exchange(API_1_0_MESSAGES + "/" + messageId, HttpMethod.DELETE, null, responseType);
    }

    public <T> ResponseEntity<T> getNewMessagesCount(long messageId, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_MESSAGES + "/" + messageId + "?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewMessagesCountOfUser(long messageId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/messages/" + messageId + "?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewMessages(long messageId, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_MESSAGES + "/" + messageId + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewMessagesOfUser(long messageId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/messages/" + messageId + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getOldMessages(long messageId, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_MESSAGES + "/" + messageId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getOldMessagesOfUser(long messageId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/messages/" + messageId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getMessagesOfUser(String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/messages";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getMessages(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_1_0_MESSAGES, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> postMessage(Message message, Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_MESSAGES, message, responseType);
    }

    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }
}
