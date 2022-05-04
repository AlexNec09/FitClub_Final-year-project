package com.project.fitclub.shared;

import com.project.fitclub.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserSecurityUtil {
    public static User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object obj = auth.getPrincipal();
        if (obj == null) return null;
        if (!(obj instanceof User)) return null;
        return (User) obj;
    }

}
