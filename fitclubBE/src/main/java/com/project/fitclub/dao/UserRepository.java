package com.project.fitclub.dao;

import com.project.fitclub.model.User;
import com.project.fitclub.validation.verificationToken.VerificationToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByEmail(String email);

    Optional<User> findById(Long id);

    @Query("FROM User u WHERE u.displayName LIKE %:searchText% OR u.username LIKE %:searchText% ORDER BY u.username, u.displayName ASC")
    Page<User> findAllUsers(@Param("searchText") String searchText, Pageable page);

    User findUserByVerificationToken(VerificationToken verificationToken);

    Page<User> findByUsernameNot(String username, Pageable page);
}
