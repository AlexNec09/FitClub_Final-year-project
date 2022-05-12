package com.project.fitclub.service;

import com.project.fitclub.dao.RoleRepository;
import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.error.NotFoundHandler;
import com.project.fitclub.model.Role;
import com.project.fitclub.model.RoleName;
import com.project.fitclub.model.User;
import com.project.fitclub.model.vm.UserUpdateVM;
import com.project.fitclub.security.JwtTokenProvider;
import com.project.fitclub.security.UserPrincipal;
import com.project.fitclub.shared.EmailSenderService;
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

    JwtTokenProvider jwtTokenProvider;

    EmailSenderService emailSender;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                       FileService fileService, JwtTokenProvider jwtTokenProvider, EmailSenderService emailSender) {
        super();
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailSender = emailSender;
    }

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerificationToken(jwtTokenProvider.generateEmailVerificationToken(user.getUsername()));
        user.setEmailVerificationStatus(false);
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER);
        user.setRoles(Collections.singleton(userRole));
        User savedUser = userRepository.save(user);
        emailSender.verifyEmail(savedUser);
        return savedUser;
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

    public Page<User> getUsers(UserPrincipal loggedInUser, Pageable pageable) {
        if (loggedInUser != null) {
            return userRepository.findByUsernameNot(loggedInUser.getUsername(), pageable);
        }
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
    public void follow(long id, Long currentUserId) {
        User targetUser = getById(id);
        User currentUserInDB = getById(currentUserId);

        targetUser.getFollowedBy().add(currentUserInDB);
        userRepository.save(targetUser);

        currentUserInDB.getFollows().add(targetUser);
        userRepository.save(currentUserInDB);
    }

    @Transactional
    public void unfollow(long id, Long currentUserId) {
        User targetUser = getById(id);
        User currentUserInDB = getById(currentUserId);

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

    public Page<User> findAll(String searchText, Pageable page) {
        return userRepository.findAllUsers(searchText, page);
    }

    public boolean resendEmailById(long id) {
        boolean returnValue = false;
        try {
            User userDB = userRepository.findById(id).get();
            userDB.setEmailVerificationToken(new JwtTokenProvider().generateEmailVerificationToken(userDB.getUsername()));
            userRepository.save(userDB);

            emailSender.verifyEmail(userDB);
            returnValue = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public boolean verifyEmailToken(String token) {
        boolean returnValue = false;
        System.out.println("my token: " + token);
        User userDB = userRepository.findUserByEmailVerificationToken(token);
        System.out.println("userDB: " + userDB.getUsername());
        // verify token expired date
        boolean isTokenValid = jwtTokenProvider.validateToken(token);
        System.out.println("isTokenValid: " + isTokenValid);
        if (isTokenValid) {
            System.out.println("HERE");
            userDB.setEmailVerificationToken(null);
            userDB.setEmailVerificationStatus(Boolean.TRUE);
            userRepository.save(userDB);
            returnValue = true;
        }

        return returnValue;
    }
}
