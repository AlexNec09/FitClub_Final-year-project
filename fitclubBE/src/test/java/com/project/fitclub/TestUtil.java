package com.project.fitclub;

import com.project.fitclub.model.Post;
import com.project.fitclub.model.User;
import com.project.fitclub.security.payload.LoginRequest;
import com.project.fitclub.security.payload.PostRequest;

public class TestUtil {

    public static User createValidUser() {
        User user = new User();
        user.setUsername("test-user");
        user.setDisplayName("test-display");
        user.setEmail("test.email@mail.com");
        user.setPassword("P4ssword");
        user.setImage("profile-image.png");
        return user;
    }

    public static User createValidUser(String username) {
        User user = createValidUser();
        user.setUsername(username);
        user.setEmail(username + ".email@mail.com");
        return user;
    }

    public static LoginRequest createLoginUser() {
        LoginRequest userForLogin = new LoginRequest();
        userForLogin.setUsername("test-user");
        userForLogin.setPassword("P4ssword");
        return userForLogin;
    }

    public static Post createValidPost() {
        Post post = new Post();
        post.setContent("test content for the test post");
        return post;
    }

    public static PostRequest createPostRequest() {
        PostRequest postRequest = new PostRequest();
        postRequest.setContent("test content for the test post");
        return postRequest;
    }
}
