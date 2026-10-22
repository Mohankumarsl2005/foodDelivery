
package com.foodDelivery.restaurant;

import java.time.LocalDateTime;

public record RestaurantResponse(
        Long id,
        String name,
        String description,
        String address,
        String phoneNumber,
        String cuisineType,
        boolean open,
        LocalDateTime createdAt
) {
    public static RestaurantResponse from(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getAddress(),
                restaurant.getPhoneNumber(),
                restaurant.getCuisineType(),
                restaurant.isOpen(),
                restaurant.getCreatedAt()
        );
    }
}
