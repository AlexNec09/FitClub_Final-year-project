package com.fitclub.fitclub.controller;

import com.fitclub.fitclub.model.Entity.User;
import com.fitclub.fitclub.shared.CurrentUser;

import com.fitclub.fitclub.model.UserVM;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class LoginController {

    @PostMapping("/api/1.0/login")
    UserVM handleLogin(@CurrentUser User loggedInUser) {
        return UserVM.createUserVM(loggedInUser);
    }
}
