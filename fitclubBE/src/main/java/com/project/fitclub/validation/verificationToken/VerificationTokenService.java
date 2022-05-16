package com.project.fitclub.validation.verificationToken;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.User;
import com.project.fitclub.security.JwtTokenProvider;
import com.project.fitclub.service.UserService;
import com.project.fitclub.shared.EmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    public boolean verifyChangeEmailToken(String token) {
        boolean isTokenValid = jwtTokenProvider.validateToken(token);

        if (isTokenValid) {
            VerificationToken changeEmailToken = verificationTokenRepository.findByEmailToken(token);
            changeEmailToken.setEmailToken(null);
            if (changeEmailToken.getPasswordToken() == null) {
                User userDB = changeEmailToken.getUser();
                deleteTokenById(changeEmailToken);
                userDB.setVerificationToken(null);
            }
            return true;
        }
        return false;
    }

    public boolean verifyChangePasswordToken(String token) {
        boolean isTokenValid = jwtTokenProvider.validateToken(token);

        if (isTokenValid) {
            VerificationToken changeEmailToken = verificationTokenRepository.findByPasswordToken(token);
            changeEmailToken.setEmailToken(null);
            if (changeEmailToken.getPasswordToken() == null) {
                User userDB = changeEmailToken.getUser();
                deleteTokenById(changeEmailToken);
                userDB.setVerificationToken(null);
            }
            return true;
        }
        return false;
    }

    public boolean changePasswordById(long id) {
        try {
            User userDB = userRepository.findById(id).get();

            VerificationToken userToken = verificationTokenRepository.findByUser(userDB);
            if (userToken == null) {
                userToken = new VerificationToken(userDB);
            }
            userToken.setPasswordToken(new JwtTokenProvider().generateVerificationToken(userDB.getUsername()));
            saveToken(userToken);

//            userDB.setVerificationToken(verificationToken);
//            userRepository.save(userDB);

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
            userToken.setEmailToken(new JwtTokenProvider().generateVerificationToken(userDB.getUsername()));
            saveToken(userToken);

//            userDB.setVerificationToken(verificationToken);
//            userRepository.save(userDB);

            emailSender.changeEmail(userToken, userDB);
            returnValue = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }
}
