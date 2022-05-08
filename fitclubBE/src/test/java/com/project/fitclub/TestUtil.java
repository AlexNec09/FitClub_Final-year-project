package com.project.fitclub;

import com.project.fitclub.model.Message;
import com.project.fitclub.model.User;
import com.project.fitclub.security.payload.LoginRequest;

public class TestUtil {

    public static User createValidUser() {
        User user = new User();
        user.setUsername("test-user");
        user.setDisplayName("test-display");
        user.setPassword("P4ssword");
        user.setImage("profile-image.png");
        return user;
    }

    public static User createValidUser(String username) {
        User user = createValidUser();
        user.setUsername(username);
        return user;
    }

    public static LoginRequest createLoginUser() {
        LoginRequest userForLogin = new LoginRequest();
        userForLogin.setUsername("test-user");
        userForLogin.setPassword("P4ssword");
        return userForLogin;
    }

    public static Message createValidMessage() {
        Message message = new Message();
        message.setContent("test content for the test message");
        return message;
    }
}
