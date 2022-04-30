package com.fitclub.fitclub.user;

import com.fitclub.fitclub.shared.CurrentUser;

import com.fitclub.fitclub.user.vm.UserVM;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class LoginController {

    @PostMapping("/api/1.0/login")
    UserVM handleLogin(@CurrentUser User loggedInUser) {
        return new UserVM(loggedInUser);
    }
}
