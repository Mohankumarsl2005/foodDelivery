package com.foodDelivery.orderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long orderId,
        Long foodItemId,
        String foodItemName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
