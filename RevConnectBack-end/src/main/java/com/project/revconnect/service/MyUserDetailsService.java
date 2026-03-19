package com.project.revconnect.service;

import com.project.revconnect.model.User;
import com.project.revconnect.model.UserPrincipal;
import com.project.revconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String identifier = username == null ? null : username.trim();
        if (identifier == null || identifier.isEmpty()) {
            throw new UsernameNotFoundException("User identifier is empty");
        }

        User user = repo.findByUsername(identifier);
        if (user == null) {
            user = repo.findByEmailIgnoreCase(identifier);
        }

        if (user == null) {
            System.out.println("User not found: " + identifier);
            throw new UsernameNotFoundException("User not found: " + identifier);
        }

        return new UserPrincipal(user);
    }
}
