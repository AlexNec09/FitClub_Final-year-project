package com.project.fitclub.controller;

import com.project.fitclub.error.ApiError;
import com.project.fitclub.model.User;
import com.project.fitclub.model.vm.UserVM;
import com.project.fitclub.security.CustomUserDetailsService;
import com.project.fitclub.shared.CurrentUser;
import com.project.fitclub.shared.JwtUtils;
import com.project.fitclub.shared.request.LoginRequest;
import com.project.fitclub.shared.response.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/1.0")
@CrossOrigin
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtUtils utils;

//    @PostMapping("/api/1.0/login")
//    UserVM handleLogin(@CurrentUser User loggedInUser) {
//        return UserVM.createUserVM(loggedInUser);
//    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws Exception {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())

            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (BadCredentialsException err) {
            throw new Exception("Incorrect username or password!", err);
        }

        UserPrincipal user = customUserDetailsService.loadUserByUsername(loginRequest.getUsername());
        final String jwt = utils.generateToken(user);
        user.setJwt(jwt);

        return ResponseEntity.ok(user);
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiError handleAccessDeniedException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        return new ApiError(401, "Access error", "/api/1.0/login");
    }
}

