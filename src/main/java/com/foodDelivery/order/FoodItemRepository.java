package com.foodDelivery.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodItemRepository
        extends JpaRepository<FoodItem, Long> {
}
