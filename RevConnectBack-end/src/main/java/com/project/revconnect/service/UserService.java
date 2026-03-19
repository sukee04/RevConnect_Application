package com.project.revconnect.service;

import com.project.revconnect.dto.ForgotPasswordRequestDTO;
import com.project.revconnect.dto.LoginRequestDTO;
import com.project.revconnect.dto.RegisterRequest;
import com.project.revconnect.dto.ResetPasswordRequestDTO;
import com.project.revconnect.model.PasswordResetOtp;
import com.project.revconnect.model.User;
import com.project.revconnect.model.UserPrincipal;
import com.project.revconnect.repository.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int OTP_RESEND_COOLDOWN_SECONDS = 60;
    private static final int OTP_MAX_ATTEMPTS = 5;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetOtpRepository passwordResetOtpRepository;

    @Autowired
    private PostViewEventRepository postViewEventRepository;

    @Autowired
    private StoryViewEventRepository storyViewEventRepository;

    @Autowired
    private CreatorSubscriptionRepository creatorSubscriptionRepository;

    @Autowired
    private BusinessCollaborationProposalRepository businessCollaborationProposalRepository;

    @Autowired
    private BrandCollaborationApplicationRepository brandCollaborationApplicationRepository;

    @Autowired
    private BrandCollaborationOpportunityRepository brandCollaborationOpportunityRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.mail.brand.name:RevConnect}")
    private String mailBrandName;

    @Value("${app.mail.brand.primary-color:#0f766e}")
    private String mailPrimaryColor;

    @Value("${app.mail.brand.secondary-color:#0f172a}")
    private String mailSecondaryColor;

    @Value("${app.mail.brand.accent-color:#f59e0b}")
    private String mailAccentColor;

    @Value("${app.mail.enable-styles:true}")
    private boolean mailStylesEnabled;

    private final Map<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();

    private static class PendingRegistration {
        private String email;
        private String username;
        private com.project.revconnect.enums.Handlers role;
        private String passwordHash;
        private String otpHash;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private int attempts;
    }


    public String registerUser(RegisterRequest request) {
        if (request.getPassword() == null || request.getPassword().length() < PASSWORD_MIN_LENGTH) {
            return "PASSWORD_TOO_SHORT";
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return "PASSWORD_MISMATCH";
        }

        if (userRepository.findByUsername(request.getUsername()) != null) {
            return "USERNAME_EXISTS";
        }

        if (userRepository.findByEmailIgnoreCase(request.getEmail()) != null) {
            return "EMAIL_EXISTS";
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);

        return "SUCCESS";
    }

    public String requestRegistrationOtp(RegisterRequest request) {
        String email = normalizeEmail(request != null ? request.getEmail() : null);
        String username = request != null ? request.getUsername() : null;
        String password = request != null ? request.getPassword() : null;
        String confirmPassword = request != null ? request.getConfirmPassword() : null;
        com.project.revconnect.enums.Handlers role = request != null ? request.getRole() : null;

        if (email == null) {
            return "EMAIL_REQUIRED";
        }
        if (username == null || username.trim().isEmpty()) {
            return "USERNAME_REQUIRED";
        }
        if (role == null) {
            return "ROLE_REQUIRED";
        }
        if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
            return "PASSWORD_TOO_SHORT";
        }
        if (!password.equals(confirmPassword)) {
            return "PASSWORD_MISMATCH";
        }

        if (userRepository.findByUsername(username.trim()) != null) {
            return "USERNAME_EXISTS";
        }
        if (userRepository.findByEmailIgnoreCase(email) != null) {
            return "EMAIL_EXISTS";
        }

        LocalDateTime now = LocalDateTime.now();
        PendingRegistration existing = pendingRegistrations.get(email);
        if (existing != null && existing.createdAt != null
                && existing.createdAt.isAfter(now.minusSeconds(OTP_RESEND_COOLDOWN_SECONDS))) {
            return "OTP_SENT";
        }

        String otp = generateOtp();
        PendingRegistration pending = new PendingRegistration();
        pending.email = email;
        pending.username = username.trim();
        pending.role = role;
        pending.passwordHash = passwordEncoder.encode(password);
        pending.otpHash = passwordEncoder.encode(otp);
        pending.createdAt = now;
        pending.expiresAt = now.plusMinutes(OTP_EXPIRY_MINUTES);
        pending.attempts = 0;
        pendingRegistrations.put(email, pending);

        boolean sent = sendRegistrationOtpEmail(email, otp);
        if (!sent) {
            pendingRegistrations.remove(email);
            return "OTP_SEND_FAILED";
        }

        return "OTP_SENT";
    }

    @Transactional
    public String verifyRegistrationOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return "EMAIL_REQUIRED";
        }
        if (otp == null || otp.trim().isEmpty()) {
            return "OTP_REQUIRED";
        }

        PendingRegistration pending = pendingRegistrations.get(normalizedEmail);
        if (pending == null) {
            return "OTP_INVALID_OR_EXPIRED";
        }

        if (pending.expiresAt == null || pending.expiresAt.isBefore(LocalDateTime.now())) {
            pendingRegistrations.remove(normalizedEmail);
            return "OTP_INVALID_OR_EXPIRED";
        }

        if (pending.attempts >= OTP_MAX_ATTEMPTS) {
            pendingRegistrations.remove(normalizedEmail);
            return "OTP_TOO_MANY_ATTEMPTS";
        }

        if (!passwordEncoder.matches(otp.trim(), pending.otpHash)) {
            pending.attempts += 1;
            if (pending.attempts >= OTP_MAX_ATTEMPTS) {
                pendingRegistrations.remove(normalizedEmail);
                return "OTP_TOO_MANY_ATTEMPTS";
            }
            pendingRegistrations.put(normalizedEmail, pending);
            return "OTP_INVALID";
        }

        if (userRepository.findByUsername(pending.username) != null) {
            pendingRegistrations.remove(normalizedEmail);
            return "USERNAME_EXISTS";
        }
        if (userRepository.findByEmailIgnoreCase(normalizedEmail) != null) {
            pendingRegistrations.remove(normalizedEmail);
            return "EMAIL_EXISTS";
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setUsername(pending.username);
        user.setPassword(pending.passwordHash);
        user.setRole(pending.role);
        userRepository.save(user);
        pendingRegistrations.remove(normalizedEmail);
        return "SUCCESS";
    }


    public String verify(LoginRequestDTO request) {
        try {
            if (request == null || request.getUsername() == null || request.getPassword() == null) {
                return "FAIL";
            }

            String identifier = request.getUsername().trim();
            if (identifier.isEmpty()) {
                return "FAIL";
            }

            User existingUser = findByUsername(identifier);
            if (existingUser != null && !existingUser.isActive()) {
                return "ACCOUNT_DEACTIVATED";
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            identifier,
                            request.getPassword()));

            if (authentication.isAuthenticated()) {
                User authenticatedUser = findByUsername(identifier);
                if (authenticatedUser == null) {
                    return "FAIL";
                }
                return jwtService.generateToken(authenticatedUser.getUsername(), authenticatedUser.getId());
            }
        } catch (AuthenticationException e) {
            return "FAIL";
        }

        return "FAIL";
    }

    public String reactivateAccount(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            return "USERNAME_REQUIRED";
        }
        if (password == null || password.isBlank()) {
            return "PASSWORD_REQUIRED";
        }

        User user = findByUsername(usernameOrEmail.trim());
        if (user == null) {
            return "INVALID_CREDENTIALS";
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return "INVALID_CREDENTIALS";
        }

        if (user.isActive()) {
            return "ACCOUNT_ALREADY_ACTIVE";
        }

        user.setActive(true);
        userRepository.save(user);
        return "ACCOUNT_REACTIVATED";
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            user = userRepository.findByEmailIgnoreCase(username);
        }
        return user;
    }


    public String requestPasswordResetOtp(ForgotPasswordRequestDTO request) {
        String email = normalizeEmail(request != null ? request.getEmail() : null);
        if (email == null) {
            return "EMAIL_REQUIRED";
        }

        User user = userRepository.findByEmailIgnoreCase(email);
        if (user == null) {

            return "OTP_SENT";
        }

        LocalDateTime now = LocalDateTime.now();
        Optional<PasswordResetOtp> latestOtp = passwordResetOtpRepository.findTopByUserOrderByCreatedAtDesc(user);
        if (latestOtp.isPresent() &&
                latestOtp.get().getCreatedAt().isAfter(now.minusSeconds(OTP_RESEND_COOLDOWN_SECONDS))) {
            return "OTP_SENT";
        }

        invalidateActiveOtps(user);

        String otp = generateOtp();
        PasswordResetOtp passwordResetOtp = new PasswordResetOtp();
        passwordResetOtp.setUser(user);
        passwordResetOtp.setOtpHash(passwordEncoder.encode(otp));
        passwordResetOtp.setCreatedAt(now);
        passwordResetOtp.setExpiresAt(now.plusMinutes(OTP_EXPIRY_MINUTES));
        passwordResetOtp.setUsed(false);
        passwordResetOtp.setAttempts(0);
        passwordResetOtpRepository.save(passwordResetOtp);

        boolean isSent = sendPasswordResetEmail(email, otp);
        if (!isSent) {
            passwordResetOtp.setUsed(true);
            passwordResetOtpRepository.save(passwordResetOtp);
            return "OTP_SEND_FAILED";
        }

        return "OTP_SENT";
    }

    public String verifyPasswordResetOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return "EMAIL_REQUIRED";
        }
        if (otp == null || otp.trim().isEmpty()) {
            return "OTP_REQUIRED";
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail);
        if (user == null) {
            return "OTP_INVALID_OR_EXPIRED";
        }

        Optional<PasswordResetOtp> optionalOtp = passwordResetOtpRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user);
        if (optionalOtp.isEmpty()) {
            return "OTP_INVALID_OR_EXPIRED";
        }

        PasswordResetOtp activeOtp = optionalOtp.get();
        if (activeOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            activeOtp.setUsed(true);
            passwordResetOtpRepository.save(activeOtp);
            return "OTP_INVALID_OR_EXPIRED";
        }

        if (activeOtp.getAttempts() >= OTP_MAX_ATTEMPTS) {
            activeOtp.setUsed(true);
            passwordResetOtpRepository.save(activeOtp);
            return "OTP_TOO_MANY_ATTEMPTS";
        }

        if (!passwordEncoder.matches(otp.trim(), activeOtp.getOtpHash())) {
            activeOtp.setAttempts(activeOtp.getAttempts() + 1);
            if (activeOtp.getAttempts() >= OTP_MAX_ATTEMPTS) {
                activeOtp.setUsed(true);
                passwordResetOtpRepository.save(activeOtp);
                return "OTP_TOO_MANY_ATTEMPTS";
            }
            passwordResetOtpRepository.save(activeOtp);
            return "OTP_INVALID";
        }

        return "OTP_VERIFIED";
    }

    @Transactional
    public String resetPasswordWithOtp(ResetPasswordRequestDTO request) {
        String normalizedEmail = normalizeEmail(request != null ? request.getEmail() : null);
        String otp = request != null ? request.getOtp() : null;
        String newPassword = request != null ? request.getNewPassword() : null;
        String confirmPassword = request != null ? request.getConfirmPassword() : null;

        if (normalizedEmail == null) {
            return "EMAIL_REQUIRED";
        }
        if (otp == null || otp.trim().isEmpty()) {
            return "OTP_REQUIRED";
        }
        if (newPassword == null || newPassword.length() < PASSWORD_MIN_LENGTH) {
            return "PASSWORD_TOO_SHORT";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "PASSWORD_MISMATCH";
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail);
        if (user == null) {
            return "OTP_INVALID_OR_EXPIRED";
        }

        Optional<PasswordResetOtp> optionalOtp = passwordResetOtpRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user);
        if (optionalOtp.isEmpty()) {
            return "OTP_INVALID_OR_EXPIRED";
        }

        PasswordResetOtp activeOtp = optionalOtp.get();
        if (activeOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            activeOtp.setUsed(true);
            passwordResetOtpRepository.save(activeOtp);
            return "OTP_INVALID_OR_EXPIRED";
        }

        if (activeOtp.getAttempts() >= OTP_MAX_ATTEMPTS) {
            activeOtp.setUsed(true);
            passwordResetOtpRepository.save(activeOtp);
            return "OTP_TOO_MANY_ATTEMPTS";
        }

        if (!passwordEncoder.matches(otp.trim(), activeOtp.getOtpHash())) {
            activeOtp.setAttempts(activeOtp.getAttempts() + 1);
            if (activeOtp.getAttempts() >= OTP_MAX_ATTEMPTS) {
                activeOtp.setUsed(true);
                passwordResetOtpRepository.save(activeOtp);
                return "OTP_TOO_MANY_ATTEMPTS";
            }
            passwordResetOtpRepository.save(activeOtp);
            return "OTP_INVALID";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        activeOtp.setUsed(true);
        passwordResetOtpRepository.save(activeOtp);
        return "PASSWORD_RESET_SUCCESS";
    }


    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal && userPrincipal.getId() != null) {
            return userRepository.findById(userPrincipal.getId()).orElse(null);
        }

        String identifier = auth.getName();
        if (identifier == null) {
            return null;
        }

        User user = userRepository.findByUsername(identifier);
        if (user == null) {
            user = userRepository.findByEmailIgnoreCase(identifier);
        }
        return user;
    }

    public User getCurrentUser() {
        return getLoggedInUser();
    }

    public String generateTokenForCurrentUser() {
        User user = getLoggedInUser();
        if (user == null) {
            return null;
        }
        return jwtService.generateToken(user.getUsername(), user.getId());
    }

    public String updatePassword(String oldPassword, String newPassword) {
        User user = getLoggedInUser();
        if (user == null)
            return "USER_NOT_FOUND";

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return "Old password does not match";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "Password updated successfully";
    }

    public String updateAccountDetails(String newUsername, String newEmail) {
        User user = getLoggedInUser();
        if (user == null)
            return "USER_NOT_FOUND";

        if (newUsername != null && !newUsername.equals(user.getUsername())) {
            if (userRepository.findByUsername(newUsername) != null) {
                return "USERNAME_EXISTS";
            }
            user.setUsername(newUsername);
        }

        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            if (userRepository.findByEmailIgnoreCase(newEmail) != null) {
                return "EMAIL_EXISTS";
            }
            user.setEmail(newEmail);
        }

        userRepository.save(user);
        return "Account details updated successfully";
    }

    public String deactivateAccount(String password) {
        User user = getLoggedInUser();
        if (user == null)
            return "USER_NOT_FOUND";

        if (password == null || password.isBlank()) {
            return "PASSWORD_REQUIRED";
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return "INVALID_PASSWORD";
        }

        user.setActive(false);
        userRepository.save(user);
        return "Account deactivated successfully";
    }

    @Transactional
    public String deleteCurrentUser(String password) {
        User user = getLoggedInUser();
        if (user == null)
            return "USER_NOT_FOUND";

        if (password == null || password.isBlank()) {
            return "PASSWORD_REQUIRED";
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return "INVALID_PASSWORD";
        }

        cleanupUserDependencies(user.getId());
        userRepository.delete(user);
        return "Account deleted successfully";
    }

    private void cleanupUserDependencies(Long userId) {

        postViewEventRepository.deleteByViewer_Id(userId);
        postViewEventRepository.deleteByPost_User_Id(userId);
        storyViewEventRepository.deleteByViewer_Id(userId);
        storyViewEventRepository.deleteByStory_User_Id(userId);


        passwordResetOtpRepository.deleteByUser_Id(userId);
        creatorSubscriptionRepository.deleteByCreator_IdOrSubscriber_Id(userId, userId);


        brandCollaborationApplicationRepository.deleteByCreator_Id(userId);
        brandCollaborationApplicationRepository.deleteByOpportunity_BusinessUser_Id(userId);
        businessCollaborationProposalRepository.deleteByBusinessUser_IdOrCreatorUser_Id(userId, userId);
        brandCollaborationOpportunityRepository.deleteByBusinessUser_Id(userId);


        postRepository.clearCollaboratorByUserId(userId);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private void invalidateActiveOtps(User user) {
        List<PasswordResetOtp> activeOtps = passwordResetOtpRepository.findByUserAndUsedFalse(user);
        for (PasswordResetOtp activeOtp : activeOtps) {
            activeOtp.setUsed(true);
        }
        if (!activeOtps.isEmpty()) {
            passwordResetOtpRepository.saveAll(activeOtps);
        }
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private boolean sendPasswordResetEmail(String email, String otp) {
        return sendOtpEmail(email, otp, "Password Reset");
    }

    private boolean sendRegistrationOtpEmail(String email, String otp) {
        return sendOtpEmail(email, otp, "Registration");
    }

    private boolean sendOtpEmail(String email, String otp, String flowLabel) {
        if (!isMailConfigurationValid()) {
            LOGGER.error("Unable to send {} OTP email: SMTP credentials are missing. Configure spring.mail.username and spring.mail.password.", flowLabel);
            return false;
        }

        try {
            String normalizedFlow = flowLabel == null ? "Verification" : flowLabel.trim();
            String subject = mailBrandName + " " + normalizedFlow + " OTP";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(email);
            helper.setSubject(subject);

            String fromAddress = resolveFromAddress();
            if (fromAddress != null) {
                helper.setFrom(fromAddress);
            }

            helper.setText(
                    buildOtpText(normalizedFlow, otp),
                    buildOtpHtml(normalizedFlow, otp));

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to send {} OTP email to {}: {}", flowLabel, email, e.getMessage(), e);
            return false;
        }
    }

    private boolean isMailConfigurationValid() {
        return mailUsername != null && !mailUsername.trim().isEmpty()
                && mailPassword != null && !mailPassword.trim().isEmpty();
    }

    private String resolveFromAddress() {
        if (mailFrom != null && !mailFrom.trim().isEmpty()) {
            return mailFrom.trim();
        }
        if (mailUsername != null && !mailUsername.trim().isEmpty()) {
            return mailUsername.trim();
        }
        return null;
    }

    private String buildOtpText(String flowLabel, String otp) {
        return "Your OTP for " + mailBrandName + " " + flowLabel.toLowerCase() + " is: " + otp + "\n\n"
                + "This OTP is valid for " + OTP_EXPIRY_MINUTES + " minutes.\n"
                + "If you did not request this, please ignore this email.";
    }

    private String buildOtpHtml(String flowLabel, String otp) {
        String brand = escapeHtml(mailBrandName);
        String safeFlow = escapeHtml(flowLabel);
        String safeOtp = escapeHtml(otp);

        if (!mailStylesEnabled) {
            return "<html><body>"
                    + "<h2>" + brand + "</h2>"
                    + "<p>Your OTP for " + safeFlow + " is <strong>" + safeOtp + "</strong>.</p>"
                    + "<p>This OTP is valid for " + OTP_EXPIRY_MINUTES + " minutes.</p>"
                    + "<p>If you did not request this, please ignore this email.</p>"
                    + "</body></html>";
        }

        String safePrimary = escapeHtml(mailPrimaryColor);
        String safeSecondary = escapeHtml(mailSecondaryColor);
        String safeAccent = escapeHtml(mailAccentColor);

        return "<html><body style=\"margin:0;padding:0;background:#f4f7fb;font-family:Arial,sans-serif;color:#0f172a;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"padding:24px 12px;\">"
                + "<tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:560px;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #e5e7eb;\">"
                + "<tr><td style=\"background:" + safeSecondary + ";padding:20px 24px;color:#ffffff;\">"
                + "<div style=\"font-size:22px;font-weight:700;line-height:1.3;\">" + brand + "</div>"
                + "<div style=\"font-size:13px;opacity:0.88;margin-top:4px;\">" + safeFlow + " Verification</div>"
                + "</td></tr>"
                + "<tr><td style=\"padding:24px;\">"
                + "<p style=\"margin:0 0 12px 0;font-size:15px;line-height:1.6;\">Use the OTP below to continue your " + safeFlow.toLowerCase() + ".</p>"
                + "<div style=\"margin:16px 0 14px 0;padding:14px 16px;border:1px solid " + safePrimary + ";border-radius:10px;"
                + "background:#f8fffd;font-size:30px;letter-spacing:6px;font-weight:700;color:" + safePrimary + ";text-align:center;\">"
                + safeOtp + "</div>"
                + "<p style=\"margin:0 0 8px 0;font-size:13px;color:#475569;\">This OTP is valid for <strong>" + OTP_EXPIRY_MINUTES + " minutes</strong>.</p>"
                + "<p style=\"margin:0;font-size:13px;color:#64748b;\">If you did not request this, ignore this email.</p>"
                + "</td></tr>"
                + "<tr><td style=\"padding:14px 24px;background:#f8fafc;border-top:1px solid #e5e7eb;\">"
                + "<span style=\"font-size:12px;color:#64748b;\">Secure with </span>"
                + "<span style=\"font-size:12px;color:" + safeAccent + ";font-weight:600;\">" + brand + "</span>"
                + "</td></tr>"
                + "</table>"
                + "</td></tr></table>"
                + "</body></html>";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
