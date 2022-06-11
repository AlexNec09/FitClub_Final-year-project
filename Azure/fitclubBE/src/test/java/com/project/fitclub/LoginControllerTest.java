package com.project.fitclub;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.error.ApiError;
import com.project.fitclub.model.User;
import com.project.fitclub.security.UserPrincipal;
import com.project.fitclub.security.payload.LoginRequest;
import com.project.fitclub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
public class LoginControllerTest {

    private static final String API_1_0_LOGIN = "/api/1.0/auth/login";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @BeforeEach
    public void cleanup() {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    @Test
    public void postLogin_withoutUserCredentials_receiveBadRequest() {
        ResponseEntity<UserPrincipal> response = authenticateUser(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postLogin_withIncorrectCredentials_receiveUnauthorized() {
        ResponseEntity<UserPrincipal> response = authenticateUser(new LoginRequest("test-invalid-user", "P4ssword12@"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void postLogin_withValidCredentials_receiveOk() {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postLogin_withValidCredentials_receiveLoggedInUserId() {
        User inDB = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        UserPrincipal body = response.getBody();
        assert body != null;
        Long id = body.getId();
        assertThat(id).isEqualTo(inDB.getId());
    }

    @Test
    public void postLogin_withValidCredentials_receiveLoggedInUsersImage() {
        User inDB = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        UserPrincipal body = response.getBody();
        assert body != null;
        String image = body.getImage();
        assertThat(image).isEqualTo(inDB.getImage());
    }

    @Test
    public void postLogin_withValidCredentials_receiveLoggedInUsersDisplayName() {
        User inDB = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        UserPrincipal body = response.getBody();
        assert body != null;
        String displayName = body.getDisplayName();
        assertThat(displayName).isEqualTo(inDB.getDisplayName());
    }

    @Test
    public void postLogin_withValidCredentials_receiveLoggedInUsersUsername() {
        User inDB = userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        UserPrincipal body = response.getBody();
        assert body != null;
        String username = body.getUsername();
        assertThat(username).isEqualTo(inDB.getUsername());
    }

    @Test
    public void postLogin_withValidCredentials_notReceiveLoggedInUsersPassword() {
        userService.saveWithoutSendingEmail(TestUtil.createValidUser("test-user"));
        LoginRequest loggingUser = TestUtil.createLoginUser();
        ResponseEntity<UserPrincipal> response = authenticateUser(loggingUser);

        UserPrincipal body = response.getBody();
        assertThat(body).hasFieldOrProperty("password");
    }

    private ResponseEntity<UserPrincipal> authenticateUser(LoginRequest loggingUser) {
        ResponseEntity<UserPrincipal> userPrincipalResponseEntity = testRestTemplate.postForEntity("/api/1.0/auth/login", loggingUser, UserPrincipal.class);
        return userPrincipalResponseEntity;
    }

    @Test
    public void postLogin_withoutUserCredentials_receiveApiError() {
        ResponseEntity<ApiError> response = login(ApiError.class);
        assertThat(response.getBody().getUrl()).isEqualTo(API_1_0_LOGIN);
    }

    public <T> ResponseEntity<T> login(Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_LOGIN, null, responseType);
    }
}
