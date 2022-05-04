package com.project.fitclub.shared;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal  // getting the principal from auth object and casting to my class
public @interface CurrentUser {
}
