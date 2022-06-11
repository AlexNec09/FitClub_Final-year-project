package com.project.fitclub.validation;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    @Autowired
    UserRepository userRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        User inDB = userRepository.findByUsername(value);
        if (inDB == null) {
            return true;
        }
        return false;
    }
}
