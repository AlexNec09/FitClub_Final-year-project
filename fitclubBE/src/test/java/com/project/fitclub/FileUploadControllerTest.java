package com.project.fitclub;

import com.project.fitclub.configuration.AppConfiguration;
import com.project.fitclub.dao.FileAttachmentRepository;
import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.FileAttachment;
import com.project.fitclub.service.UserService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FileUploadControllerTest {

    private static final String API_1_0_POSTS_UPLOAD = "/api/1.0/posts/upload";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @BeforeEach
    public void init() throws IOException {
        userRepository.deleteAll();
        fileAttachmentRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_receiveOk() {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticate();
        ResponseEntity<Object> response = uploadFile(getRequestEntity(), Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void uploadFile_withImageFromUnauthorizedUser_receiveUnauthorized() {
        ResponseEntity<Object> response = uploadFile(getRequestEntity(), Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_receiveFileAttachmentWithDate() {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticate();
        ResponseEntity<FileAttachment> response = uploadFile(getRequestEntity(), FileAttachment.class);
        assertThat(response.getBody().getDate()).isNotNull();
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_receiveFileAttachmentWithRandomName() {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticate();
        ResponseEntity<FileAttachment> response = uploadFile(getRequestEntity(), FileAttachment.class);
        assertThat(response.getBody().getName()).isNotNull();
        assertThat(response.getBody().getName()).isNotEqualTo("profile.png");
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_imageSavedToFolder() {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticate();
        ResponseEntity<FileAttachment> response = uploadFile(getRequestEntity(), FileAttachment.class);
        String imagePath = appConfiguration.getFullAttachmentsPath() + "/" + response.getBody().getName();
        File storedImage = new File(imagePath);
        assertThat(storedImage.exists()).isTrue();
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_fileAttachmentSavedToDatabase() {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticate();
        uploadFile(getRequestEntity(), FileAttachment.class);
        assertThat(fileAttachmentRepository.count()).isEqualTo(1);
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_fileAttachmentStoredWithFileType() {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("user1"));
        authenticate();
        uploadFile(getRequestEntity(), FileAttachment.class);
        FileAttachment storedFile = fileAttachmentRepository.findAll().get(0);
        assertThat(storedFile.getFileType()).isEqualTo("image/png");
    }


    public <T> ResponseEntity<T> uploadFile(HttpEntity<?> requestEntity, Class<T> responseType) {
        return testRestTemplate.exchange(API_1_0_POSTS_UPLOAD, HttpMethod.POST, requestEntity, responseType);
    }

    private HttpEntity<MultiValueMap<String, Object>> getRequestEntity() {
        ClassPathResource imageResource = new ClassPathResource("test-png.png");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", imageResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        return requestEntity;
    }

    private void authenticate() {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor("user1", "P4ssword12@"));
    }
}