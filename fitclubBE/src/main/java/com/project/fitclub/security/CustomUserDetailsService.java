package com.project.fitclub.security;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.error.NotFoundHandler;
import com.project.fitclub.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        Optional<User> optUser = userRepository.findById(id);
        if (!optUser.isPresent()) {
            throw new UsernameNotFoundException("User with id " + id + " was not found.");
        }
        return UserPrincipal.create(optUser.get());
    }
}