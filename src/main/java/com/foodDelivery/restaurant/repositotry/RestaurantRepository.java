package com.foodDelivery.restaurant.repositotry;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.foodDelivery.restaurant.entity.Restaurant;

@Repository 
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
 
    List<Restaurant> findByNameContainingIgnoreCase(String name);

    List<Restaurant> findByCuisineTypeIgnoreCase(String cuisineType);

    List<Restaurant> findByIsOpenTrue();
}
