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
import com.project.fitclub.security.payload.NewPasswordRequest;
import com.project.fitclub.security.payload.UpdateEmailRequest;
import com.project.fitclub.shared.EmailSenderService;
import com.project.fitclub.validation.verificationToken.VerificationToken;
import com.project.fitclub.validation.verificationToken.VerificationTokenService;
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

    VerificationTokenService verificationTokenService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                       FileService fileService, JwtTokenProvider jwtTokenProvider, EmailSenderService emailSender, VerificationTokenService verificationTokenService) {
        super();
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailSender = emailSender;
        this.verificationTokenService = verificationTokenService;
    }

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.setEmailVerificationToken(jwtTokenProvider.generateEmailVerificationToken(user.getUsername()));
        user.setEmailVerificationStatus(false);
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER);
        user.setRoles(Collections.singleton(userRole));
//        user.setVerificationToken(newToken);
        VerificationToken newToken = new VerificationToken(user);
        newToken.setEmailToken(jwtTokenProvider.generateVerificationToken(user.getUsername()));
        user.setVerificationToken(newToken);
        User savedUser = userRepository.save(user);
        verificationTokenService.saveToken(newToken);
        emailSender.verifyEmail(savedUser);
        return savedUser;
    }

    public void updateUserWithVerificationToken(User user) {
        userRepository.save(user);
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
        try {
            User userDB = userRepository.findById(id).get();
            if (userDB.getEmailVerificationStatus()) {
                return false;
            }
            VerificationToken updatedToken = verificationTokenService.getTokenByUser(userDB);
            if (updatedToken == null) {
                VerificationToken newToken = new VerificationToken(userDB);
                updatedToken = newToken;
            }
            updatedToken.setEmailToken(new JwtTokenProvider().generateVerificationToken(userDB.getUsername()));
            verificationTokenService.saveToken(updatedToken);

//            userDB.setEmailVerificationToken(new JwtTokenProvider().generateEmailVerificationToken(userDB.getUsername()));
//            userRepository.save(userDB);

            emailSender.verifyEmail(userDB);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkTokenValidity(String tokenIdentifier, String token) {
        if (tokenIdentifier.equals("tokenForEmail")) {
            if (verificationTokenService.getTokenByEmailToken(token) == null) {
                return false;
            }
        } else if (tokenIdentifier.equals("tokenForPassword")) {
            if (verificationTokenService.getTokenByPasswordToken(token) == null) {
                return false;
            }
        } else return false;
        return jwtTokenProvider.validateToken(token);
    }

    public boolean verifyEmailToken(String token) {
        VerificationToken userToken = verificationTokenService.getTokenByEmailToken(token);
        User userDB = userToken.getUser();
        boolean isTokenValid = jwtTokenProvider.validateToken(token);
        if (isTokenValid) {
            userToken.setEmailToken(null);
            userDB.setEmailVerificationStatus(Boolean.TRUE);
            if (userToken.getPasswordToken() == null) {
                verificationTokenService.deleteTokenById(userToken);
                userDB.setVerificationToken(null);
            } else {
                verificationTokenService.saveToken(userToken);
            }
            userRepository.save(userDB);
            return true;
        }
        return false;
    }

    public boolean changeEmail(String username, UpdateEmailRequest updatedEmail) {
        try {
            User inDB = userRepository.findByUsername(username);
            inDB.setUsername(updatedEmail.getNewEmail());
            userRepository.save(inDB);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean changePassword(String username, NewPasswordRequest updatedPassword) {
        try {
            User inDB = userRepository.findByUsername(username);
            inDB.setUsername(updatedPassword.getNewPassword());
            userRepository.save(inDB);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }
}
