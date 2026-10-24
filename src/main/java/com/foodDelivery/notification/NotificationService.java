package com.foodDelivery.notification;

import com.foodDelivery.user.User;
import com.foodDelivery.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /** Intended for trusted administrative/system callers only. */
    @Transactional
    public Notification createNotification(Long userId, String message) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid user ID is required");
        }
        if (message == null || message.isBlank() || message.length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Message must contain 1 to 500 characters");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        return notificationRepository.save(new Notification(userId, message.trim()));
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(String authenticatedEmail) {
        User user = findAuthenticatedUser(authenticatedEmail);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public Notification markAsRead(Long id, String authenticatedEmail) {
        User user = findAuthenticatedUser(authenticatedEmail);
        Notification notification = notificationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found"));

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    private User findAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }
}
