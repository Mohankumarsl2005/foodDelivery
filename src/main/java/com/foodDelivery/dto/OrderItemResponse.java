package com.foodDelivery.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long foodItemId,
        String foodItemName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
