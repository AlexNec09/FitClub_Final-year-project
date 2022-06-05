package com.project.fitclub.dao;

import com.project.fitclub.model.User;
import com.project.fitclub.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long>  {

    VerificationToken findByEmailToken(String token);

    VerificationToken findByPasswordToken(String token);

    VerificationToken findByUser(User user);

    void deleteById(long id);
}
