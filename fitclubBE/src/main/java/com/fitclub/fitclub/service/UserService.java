package com.fitclub.fitclub.service;

import com.fitclub.fitclub.dao.user.UserRepository;
import com.fitclub.fitclub.error.NotFoundHandler;
import com.fitclub.fitclub.model.Entity.User;
import com.fitclub.fitclub.model.UserUpdateVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
public class UserService {

    UserRepository userRepository;

    PasswordEncoder passwordEncoder;

    FileService fileService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileService fileService) {
        super();
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
    }

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Page<User> getUsers(User loggedInUser, Pageable pageable) {
        if (loggedInUser != null) {
            return userRepository.findByUsernameNot(loggedInUser.getUsername(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    public User getByUsername(String username) {
        User inDB = userRepository.findByUsername(username);
        if (inDB == null) {
            throw new NotFoundHandler(username + " not found");
        }
        return inDB;
    }

    public User update(long id, UserUpdateVM userUpdate) throws IOException {
        User inDB = userRepository.getById(id);
        inDB.setDisplayName(userUpdate.getDisplayName());
        if (userUpdate.getImage() != null) {
            String savedImageName = fileService.saveProfileImage(userUpdate.getImage());
            fileService.deleteProfileImage(inDB.getImage());
            inDB.setImage(savedImageName);
        }
        return userRepository.save(inDB);
    }

    @Transactional // IDK
    public void follow(long id, User currentUser) {
        User targetUser = getById(id);
        User currentUserInDB = getById(currentUser.getId());

        targetUser.getFollowedBy().add(currentUserInDB);
        userRepository.save(targetUser);

        currentUserInDB.getFollows().add(targetUser);
        userRepository.save(currentUserInDB);
    }

    @Transactional
    public void unfollow(long id, User currentUser) {
        User targetUser = getById(id);
        User currentUserInDB = getById(currentUser.getId());

        targetUser.getFollowedBy().remove(currentUserInDB);
        userRepository.save(targetUser);

        currentUserInDB.getFollows().remove(targetUser);
        userRepository.save(currentUserInDB);
    }

    private User getById(long id) {
        Optional<User> inDB = userRepository.findById(id);
        if (!inDB.isPresent())
            throw new NotFoundHandler("User not found");
        return inDB.get();
    }

}
