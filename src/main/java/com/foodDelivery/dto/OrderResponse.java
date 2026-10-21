package com.foodDelivery.dto;

import com.foodDelivery.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        BigDecimal totalAmount,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}

