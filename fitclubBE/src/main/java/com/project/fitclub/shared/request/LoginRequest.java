package com.project.fitclub.shared.request;

import javax.validation.constraints.Email;
import java.io.Serializable;

public class LoginRequest implements Serializable {

    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
