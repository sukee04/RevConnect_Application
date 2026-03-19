package com.project.revconnect.repository;

import com.project.revconnect.model.PasswordResetOtp;
import com.project.revconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);
    Optional<PasswordResetOtp> findTopByUserOrderByCreatedAtDesc(User user);
    List<PasswordResetOtp> findByUserAndUsedFalse(User user);
    void deleteByUser_Id(Long userId);
}
