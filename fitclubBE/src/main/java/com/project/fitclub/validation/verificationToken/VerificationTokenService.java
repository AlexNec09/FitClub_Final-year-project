package com.project.fitclub.validation.verificationToken;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.User;
import com.project.fitclub.security.JwtTokenProvider;
import com.project.fitclub.shared.EmailSenderService;
import org.springframework.stereotype.Service;

@Service
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
}
