package com.project.fitclub.dao;

import com.project.fitclub.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    Optional<User> findById(Long id);


//    //    @Query(value="Select * from user", nativeQuery = true)
//    @Query("Select u from User u")
//    Page<UserProjection> getAllUsersProjection(Pageable pageable);

    Page<User> findByUsernameNot(String username, Pageable page);
}
