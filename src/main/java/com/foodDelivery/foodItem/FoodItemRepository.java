
package com.foodDelivery.foodItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository
        extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByRestaurant_IdAndAvailableTrueOrderByNameAsc(
            Long restaurantId
    );
}
