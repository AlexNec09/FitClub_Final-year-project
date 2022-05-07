package com.project.fitclub.service;

import com.project.fitclub.dao.RoleRepository;
import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.error.NotFoundHandler;
import com.project.fitclub.model.Role;
import com.project.fitclub.model.RoleName;
import com.project.fitclub.model.User;
import com.project.fitclub.model.vm.UserUpdateVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Service
public class UserService {

    UserRepository userRepository;

    RoleRepository roleRepository;

    PasswordEncoder passwordEncoder;

    FileService fileService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, FileService fileService) {
        super();
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
    }

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER);
        user.setRoles(Collections.singleton(userRole));
        return userRepository.save(user);
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

    public Page<User> getUsers(User loggedInUser, Pageable pageable) {
        if (loggedInUser != null)
            return userRepository.findByUsernameNot(loggedInUser.getUsername(), pageable);
        return userRepository.findAll(pageable);
    }

    public User getByUsername(String username) {
        User inDB = userRepository.findByUsername(username);
        if (inDB == null)
            throw new NotFoundHandler(username + " not found");
        return inDB;
    }

    public User getById(Long id) {
        Optional<User> inDB = userRepository.findById(id);
        if (!inDB.isPresent())
            throw new NotFoundHandler("User with id: " + id + " not found");
        return inDB.get();
    }

    @Transactional
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
