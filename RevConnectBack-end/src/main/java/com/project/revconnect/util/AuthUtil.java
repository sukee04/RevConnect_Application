package com.project.revconnect.util;

import com.project.revconnect.model.User;
import com.project.revconnect.model.UserPrincipal;
import com.project.revconnect.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {

    public static User getLoggedInUser(UserRepository userRepository) {
        String identifier = getLoggedInUsername();
        if (identifier == null) {
            return null;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal && userPrincipal.getId() != null) {
            return userRepository.findById(userPrincipal.getId()).orElse(null);
        }

        User user = userRepository.findByUsername(identifier);

        if (user == null) {
            user = userRepository.findByEmailIgnoreCase(identifier);
        }

        return user;
    }

    public static String getLoggedInUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }
}
