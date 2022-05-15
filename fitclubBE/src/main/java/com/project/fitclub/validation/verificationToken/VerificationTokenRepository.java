package com.project.fitclub.validation.verificationToken;

import com.project.fitclub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long>  {

    VerificationToken findByEmailToken(String token);

    VerificationToken findByPasswordToken(String token);

    VerificationToken findByUser(User user);

    void deleteById(long id);
}
