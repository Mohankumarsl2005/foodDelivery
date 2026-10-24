package com.foodDelivery.notification;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService service) {
        this.notificationService = service;
    }

    /** Administrative/system operation: create a notification for a specified user. */
    @PostMapping
    public ResponseEntity<Notification> createNotification(
            @RequestParam Long userId,
            @RequestParam String message) {

        Notification created = notificationService.createNotification(userId, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** A customer can only retrieve their own notifications. */
    @GetMapping("/me")
    public List<Notification> getMyNotifications(Authentication authentication) {
        return notificationService.getUserNotifications(authentication.getName());
    }

    /** A customer can only mark one of their own notifications as read. */
    @PatchMapping("/{id}/read")
    public Notification markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        return notificationService.markAsRead(id, authentication.getName());
    }
}
