package com.project.fitclub.security;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.User;
import com.project.fitclub.shared.response.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) throw new UsernameNotFoundException(username);

        return UserPrincipal.create(user);
    }

}