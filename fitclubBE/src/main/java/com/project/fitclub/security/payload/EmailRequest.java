package com.project.fitclub.security.payload;

import com.project.fitclub.validation.UniqueUsername;
import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class EmailRequest {
    @Size(min = 4, max = 255)
    @Pattern(regexp = ".+@.+\\..+", message = "Please provide a valid email address")
    @UniqueUsername
    private String newEmail;
}
