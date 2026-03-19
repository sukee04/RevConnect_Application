package com.project.revconnect.repository;

import com.project.revconnect.enums.Handlers;
import com.project.revconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByEmail(String email);
    User findByEmailIgnoreCase(String email);


    List<User> findByRole(Handlers role);

    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email);
}
