package com.project.fitclub.validation.verificationToken;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.User;
import com.project.fitclub.security.JwtTokenProvider;
import com.project.fitclub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VerificationTokenService {

    VerificationTokenRepository verificationTokenRepository;

    UserRepository userRepository;

    UserService userService;

    JwtTokenProvider jwtTokenProvider;


    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository, UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }
}
