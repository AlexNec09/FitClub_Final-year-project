package com.project.fitclub.validation.verificationToken;

import com.project.fitclub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long>  {

    void deleteVerificationTokenByChangeEmailToken(String token);

//    VerificationToken findVerificationTokenByChangeEmailToken(String token);
    VerificationToken findByChangeEmailToken(String token);
    VerificationToken findVerificationTokenByChangePasswordToken(String token);

//    VerificationToken findVerificationTokenByUserAndChangeEmailToken(User user, String changeEmailToken);

    VerificationToken findByUserAndChangeEmailTokenNotNull(User user);
}
