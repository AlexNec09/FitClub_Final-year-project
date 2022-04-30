package com.fitclub.fitclub.controller;

import com.fitclub.fitclub.model.Entity.User;
import com.fitclub.fitclub.dao.user.UniqueUsername;
import com.fitclub.fitclub.dao.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    @Autowired
    UserRepository userRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        User inDB = userRepository.findByUsername(value);
        if(inDB == null) {
            return true;
        }
        return false;
    }
}
