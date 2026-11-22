package com.foodDelivery.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        Long customerId,
        Long restaurantId,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt,
        List<Item> items
) {
    public record Item(
            Long foodItemId,
            String foodItemName,
            Integer quantity,
            BigDecimal unitPrice
    ) {}
}