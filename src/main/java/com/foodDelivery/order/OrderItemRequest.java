package com.foodDelivery.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull Long orderId,
        @NotNull Long foodItemId,
        @NotNull @Min(1) Integer quantity
) {
}
