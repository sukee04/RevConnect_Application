package com.project.revconnect.service;

import com.project.revconnect.model.Notification;
import com.project.revconnect.model.User;
import com.project.revconnect.repository.NotificationRepository;
import com.project.revconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser() {
        return com.project.revconnect.util.AuthUtil.getLoggedInUser(userRepository);
    }

    public void createNotification(User recipient, User actor, String type, Long referenceId) {
        // Don't notify yourself
        if (recipient.getId().equals(actor.getId())) return;
        Notification notification = new Notification(recipient, actor, type, referenceId);
        notificationRepository.save(notification);
    }

    public List<Notification> getMyNotifications() {
        User user = getLoggedInUser();

        return notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(user.getId());
    }

    public List<Notification> getUnreadNotifications() {
        User user = getLoggedInUser();

        return notificationRepository.findByRecipient_IdAndReadFalseOrderByCreatedAtDesc(user.getId());
    }

    public long getUnreadCount() {
        User user = getLoggedInUser();

        return notificationRepository.countByRecipient_IdAndReadFalse(user.getId());
    }

    @Transactional
    public String markAsRead(Long notificationId) {
        User user = getLoggedInUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        return "Notification marked as read";
    }

    @Transactional
    public String markAllAsRead() {
        User user = getLoggedInUser();

        notificationRepository.markAllAsReadForUser(user.getId());
        return "All notifications marked as read";
    }
}