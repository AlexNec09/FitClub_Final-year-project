package com.fitclub.fitclub;

import com.fitclub.fitclub.model.Entity.Message;
import com.fitclub.fitclub.model.Entity.User;

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

    public static Message createValidMessage() {
        Message message = new Message();
        message.setContent("test content for the test message");
        return message;
    }
}
