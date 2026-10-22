
package com.foodDelivery.order;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @GetMapping
    public List<OrderItemResponse> getAllOrderItems(
            Authentication authentication) {
        return orderItemService.getAllOrderItemsForUser(
                getAuthenticatedEmail(authentication));
    }

    @GetMapping("/{id}")
    public OrderItemResponse getOrderItemById(
            @PathVariable Long id,
            Authentication authentication) {
        return orderItemService.getOrderItemForUser(
                id, getAuthenticatedEmail(authentication));
    }

    @PostMapping
    public OrderItemResponse createOrderItem(
            @Valid @RequestBody OrderItemRequest request,
            Authentication authentication) {
        return orderItemService.createOrderItem(
                request, getAuthenticatedEmail(authentication));
    }

    @DeleteMapping("/{id}")
    public void deleteOrderItem(
            @PathVariable Long id,
            Authentication authentication) {
        orderItemService.deleteOrderItemForUser(
                id, getAuthenticatedEmail(authentication));
    }

    private String getAuthenticatedEmail(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication is required");
        }

        return authentication.getName();
    }
}
