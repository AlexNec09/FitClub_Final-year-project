package com.project.fitclub.validation.verificationToken;

import com.project.fitclub.model.User;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class VerificationToken {

    @Id
    @GeneratedValue
    private long id;

    private String changeEmailToken = "";

    private String changePasswordToken = "";

    @OneToOne
    private User user;
}
