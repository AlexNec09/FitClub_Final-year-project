package com.project.fitclub.service;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.User;
import com.project.fitclub.security.JwtTokenProvider;
import com.project.fitclub.shared.EmailSenderService;
import com.project.fitclub.model.VerificationToken;
import com.project.fitclub.dao.VerificationTokenRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@EnableScheduling
public class VerificationTokenService {

    VerificationTokenRepository verificationTokenRepository;

    UserRepository userRepository;

    JwtTokenProvider jwtTokenProvider;

    EmailSenderService emailSender;


    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository, JwtTokenProvider jwtTokenProvider,
                                    UserRepository userRepository, EmailSenderService emailSender) {
        super();
        this.verificationTokenRepository = verificationTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.emailSender = emailSender;
    }

    public void saveToken(VerificationToken verificationToken) {
        verificationTokenRepository.save(verificationToken);
    }

    public VerificationToken getTokenByUser(User user) {
        return verificationTokenRepository.findByUser(user);
    }

    public VerificationToken getTokenByEmailToken(String token) {
        return verificationTokenRepository.findByEmailToken(token);
    }

    public VerificationToken getTokenByPasswordToken(String token) {
        return verificationTokenRepository.findByPasswordToken(token);
    }

    public void deleteTokenById(VerificationToken verificationToken) {
        verificationTokenRepository.deleteById(verificationToken.getId());
    }

    @Transactional
    public User verifyChangeEmailToken(String token) {
        boolean isTokenValid = jwtTokenProvider.validateToken(token);

        if (isTokenValid) {
            VerificationToken changeEmailToken = verificationTokenRepository.findByEmailToken(token);
            changeEmailToken.setEmailToken(null);
            User userDB = changeEmailToken.getUser();
            if (changeEmailToken.getPasswordToken() == null) {
                deleteTokenById(changeEmailToken);
                userDB.setVerificationToken(null);
            }
            return userDB;
        }
        return null;
    }

    @Transactional
    public User verifyChangePasswordToken(String token) {
        boolean isTokenValid = jwtTokenProvider.validateToken(token);

        if (isTokenValid) {
            VerificationToken changePasswordToken = verificationTokenRepository.findByPasswordToken(token);
            changePasswordToken.setPasswordToken(null);
            User userDB = changePasswordToken.getUser();
            if (changePasswordToken.getEmailToken() == null) {
                deleteTokenById(changePasswordToken);
                userDB.setVerificationToken(null);
            }
            return userDB;
        }
        return null;
    }

    public boolean changePasswordById(long id) {
        try {
            User userDB = userRepository.findById(id).get();

            VerificationToken userToken = verificationTokenRepository.findByUser(userDB);
            if (userToken == null) {
                userToken = new VerificationToken(userDB);
            }
            userToken.setPasswordToken(jwtTokenProvider.generateVerificationToken(userDB.getUsername()));
            saveToken(userToken);

            emailSender.changePassword(userToken, userDB);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean changeEmailById(long id) {
        boolean returnValue = false;
        try {
            User userDB = userRepository.findById(id).get();

            VerificationToken userToken = verificationTokenRepository.findByUser(userDB);
            if (userToken == null) {
                userToken = new VerificationToken(userDB);
            }
            userToken.setEmailToken(jwtTokenProvider.generateVerificationToken(userDB.getUsername()));
            saveToken(userToken);

            emailSender.changeEmail(userToken, userDB);
            returnValue = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    @Transactional
    public void cleanExpiredTokenRecords() {
        List<VerificationToken> allTokens = verificationTokenRepository.findAll();
        for (VerificationToken tokenRecord : allTokens) {
            String emailToken = tokenRecord.getEmailToken();
            String passwordToken = tokenRecord.getPasswordToken();
            User userDB = tokenRecord.getUser();
            if (emailToken != null) {
                if (passwordToken != null) {
                    if (!jwtTokenProvider.validateToken(emailToken) && !jwtTokenProvider.validateToken(passwordToken)) {
                        deleteTokenById(tokenRecord);
                        userDB.setVerificationToken(null);
                    }
                } else if (!jwtTokenProvider.validateToken(emailToken)) {
                    deleteTokenById(tokenRecord);
                    userDB.setVerificationToken(null);
                }
            } else if (!jwtTokenProvider.validateToken(passwordToken)) {
                deleteTokenById(tokenRecord);
                userDB.setVerificationToken(null);
            }
        }
    }
}
