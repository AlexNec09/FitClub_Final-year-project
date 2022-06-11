package com.project.fitclub.dao;

import com.project.fitclub.model.Post;
import com.project.fitclub.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    Page<Post> findByUser(User user, Pageable pageable);

    Page<Post> findByUserInOrderByIdDesc(Set<User> users, Pageable pageable);
}