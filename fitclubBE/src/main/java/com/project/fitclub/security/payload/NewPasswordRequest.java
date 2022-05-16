package com.project.fitclub.security.payload;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class NewPasswordRequest {

    @NotNull
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{fitclub.constraints.password.Pattern.message}")
    String newPassword;
}
