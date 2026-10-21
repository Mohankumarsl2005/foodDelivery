package com.foodDelivery.restaurant;

public class RestaurantNotFoundException extends RuntimeException {

    public RestaurantNotFoundException(Long id) {
        super("Restaurant not found with id: " + id);
    }
}

