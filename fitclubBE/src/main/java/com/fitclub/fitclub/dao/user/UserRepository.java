package com.fitclub.fitclub.dao.user;

import com.fitclub.fitclub.model.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

//    //    @Query(value="Select * from user", nativeQuery = true)
//    @Query("Select u from User u")
//    Page<UserProjection> getAllUsersProjection(Pageable pageable);

    Page<User> findByUsernameNot(String username, Pageable page);
}
