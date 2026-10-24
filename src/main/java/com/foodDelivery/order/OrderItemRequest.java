package com.foodDelivery.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull
        @Positive
        Long foodItemId,

        @NotNull
        @Positive
        @Max(100)
        Integer quantity
) {
}

