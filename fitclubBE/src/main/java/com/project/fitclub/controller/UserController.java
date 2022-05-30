package com.project.fitclub.controller;

import com.project.fitclub.model.User;
import com.project.fitclub.model.vm.UserUpdateVM;
import com.project.fitclub.model.vm.UserVM;
import com.project.fitclub.security.UserPrincipal;
import com.project.fitclub.security.payload.NewPasswordRequest;
import com.project.fitclub.security.payload.UpdateEmailRequest;
import com.project.fitclub.service.UserService;
import com.project.fitclub.shared.CurrentUser;
import com.project.fitclub.shared.GenericResponse;
import com.project.fitclub.validation.verificationToken.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

@RestController
@RequestMapping("/api/1.0")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    VerificationTokenService verificationTokenService;

    @GetMapping("/users")
    Page<UserVM> getUsers(@CurrentUser UserPrincipal loggedInUser, Pageable page) {
        return userService.getUsers(loggedInUser, page).map(UserVM::new);
    }

    @GetMapping("/users/{username}")
    UserVM getUserByName(@PathVariable String username, @CurrentUser UserPrincipal currentUser) {
        User user = userService.getByUsername(username);
        if (currentUser != null) {
            User myUser = userService.getByUsername(currentUser.getUsername());
            return new UserVM(user, myUser);
        }
        return new UserVM(user);
    }

    @PutMapping("/users/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    UserVM updateUser(@PathVariable long id, @Valid @RequestBody(required = false) UserUpdateVM userUpdate) throws IOException {
        User updated = userService.update(id, userUpdate);
        return new UserVM(updated);
    }

    @GetMapping("/users/find/{searchText}")
    Page<UserVM> getUsers(@PathVariable String searchText, Pageable page) {
        return userService.findAll(searchText, page).map(UserVM::new);
    }

    @PostMapping(path = "/users/email-verification/confirmation/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<?> renewAndResendEmailConfirmation(@PathVariable long id) {
        try {
            boolean isSaveToDBAndSentWithSuccess = userService.resendEmailById(id);

            if (isSaveToDBAndSentWithSuccess) {
                System.out.println("Email Resending was successfully!");
                return ResponseEntity.ok(Collections.singletonMap("result", "SUCCESS"));
            } else {
                throw new Exception("Email already confirmed!");
            }
        } catch (Exception e) {
            System.out.println("An error has occurred on the process of sending email!");
            return ResponseEntity.ok(Collections.singletonMap("result", "FAIL"));
        }
    }

    @GetMapping(path = "/users/email-verification/confirmationToken/{token}")
    public ResponseEntity verifyEmailTokenForEmailVerification(@PathVariable String token) {
        try {
            boolean isVerified = userService.verifyEmailToken(token);

            if (isVerified) {
                System.out.println("SUCCESS");
                return ResponseEntity.ok(Collections.singletonMap("value", "SUCCESS"));
            }
        } catch (Exception e) {
            System.out.println("FAILING");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    @PostMapping(path = "/users/email-verification/changeEmail/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<?> changeEmailToken(@PathVariable long id) {
        try {
            boolean isSaveToDBAndSentWithSuccess = verificationTokenService.changeEmailById(id);
            if (isSaveToDBAndSentWithSuccess) {
                System.out.println("Email for changing email address was successfully!");
                return ResponseEntity.ok(Collections.singletonMap("result", "SUCCESS"));
            }

        } catch (Exception e) {
            System.out.println("An error has occurred on the process of sending email!");
            return ResponseEntity.ok(Collections.singletonMap("result", "FAIL"));
        }
        return null;
    }

    @PostMapping(path = "/users/email-verification/changeEmailToken/{token}")
    public ResponseEntity verifyEmailTokenForChangeEmail(@PathVariable String token,
                                                         @Valid @RequestBody(required = false) UpdateEmailRequest updatedEmail) {
        try {
            User user = verificationTokenService.verifyChangeEmailToken(token);
            if (user != null) {
                boolean isEmailChanged = userService.changeEmail(user.getEmail(), updatedEmail);
                if (isEmailChanged) {
                    System.out.println("SUCCESS");
                    return ResponseEntity.ok(Collections.singletonMap("value", "SUCCESS"));
                }
            } else {
                throw new Exception("Token already used or expired!");
            }
        } catch (Exception e) {
            System.out.println("FAILING");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    @GetMapping(path = "/users/isValidToken/{tokenIdentifier}/{token}")
    public ResponseEntity isTokenValid(@PathVariable String tokenIdentifier, @PathVariable String token) {
        try {
            boolean isValid = userService.checkTokenValidity(tokenIdentifier, token);

            if (isValid) {
                return ResponseEntity.ok(Collections.singletonMap("result", "VALID"));
            } else {
                throw new Exception("Token already used or expired!");
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.singletonMap("result", "INVALID"));
        }
    }

    @PostMapping(path = "/users/email-verification/changePassword/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<?> changePasswordToken(@PathVariable long id) {
        try {
            boolean isSavedToDBWithSuccess = verificationTokenService.changePasswordById(id);
            if (isSavedToDBWithSuccess) {
                System.out.println("Email for changing password was successfully!");
                return ResponseEntity.ok(Collections.singletonMap("result", "SUCCESS"));
            }

        } catch (Exception e) {
            System.out.println("An error has occurred on the process of sending email!");
            return ResponseEntity.ok(Collections.singletonMap("result", "FAIL"));
        }
        return null;
    }

    @PostMapping(path = "/users/email-verification/passwordReset/{token}")
    public ResponseEntity verifyEmailTokenForPasswordReset(@PathVariable String token,
                                                           @Valid @RequestBody(required = false) NewPasswordRequest updatedPassword) {
        try {
            User user = verificationTokenService.verifyChangePasswordToken(token);
            if (user != null) {
                boolean isPasswordChanged = userService.changePassword(user.getEmail(), updatedPassword);
                if (isPasswordChanged) {
                    System.out.println("SUCCESS");
                    return ResponseEntity.ok(Collections.singletonMap("value", "SUCCESS"));
                }
            } else {
                throw new Exception("Token already used or expired!");
            }
        } catch (Exception e) {
            System.out.println("FAILING");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    @PostMapping(path = "/users/recoverPassword")
    public ResponseEntity<?> sendEmailToRecoverPassword(@Valid @RequestBody UpdateEmailRequest userEmail) {
        boolean isEmailValid = userService.sendRecoveryEmail(userEmail.getNewEmail());
        if (isEmailValid) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
    }
}