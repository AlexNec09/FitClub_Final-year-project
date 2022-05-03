package com.fitclub.fitclub.dao.message;

import com.fitclub.fitclub.model.Entity.Message;
import com.fitclub.fitclub.model.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Set;

public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {

    Page<Message> findByUser(User user, Pageable pageable);

    Page<Message> findByUserInOrderByIdDesc(Set<User> users, Pageable pageable);
}