
package com.foodDelivery.foodItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FoodItemResponse(
        Long id,
        Long restaurantId,
        String name,
        String description,
        BigDecimal price,
        boolean available,
        LocalDateTime createdAt
) {
    public static FoodItemResponse from(FoodItem foodItem) {
        return new FoodItemResponse(
                foodItem.getId(),
                foodItem.getRestaurant().getId(),
                foodItem.getName(),
                foodItem.getDescription(),
                foodItem.getPrice(),
                foodItem.isAvailable(),
                foodItem.getCreatedAt()
        );
    }
}
