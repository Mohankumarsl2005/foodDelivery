
package com.foodDelivery.restaurant;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public Restaurant createRestaurant(Restaurant restaurant) {
        if (restaurant.getName() == null
                || restaurant.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Restaurant name is required");
        }

        // A new restaurant starts closed.
        restaurant.setOpen(false);

        return restaurantRepository.save(restaurant);
    }

    @Transactional(readOnly = true)
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new RestaurantNotFoundException(id));
    }

    @Transactional
    public Restaurant updateRestaurant(
            Long id, Restaurant updatedRestaurant) {

        Restaurant existingRestaurant = getRestaurantById(id);

        if (updatedRestaurant.getName() == null
                || updatedRestaurant.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Restaurant name is required");
        }

        existingRestaurant.setName(updatedRestaurant.getName());
        existingRestaurant.setDescription(
                updatedRestaurant.getDescription());
        existingRestaurant.setAddress(updatedRestaurant.getAddress());
        existingRestaurant.setPhoneNumber(
                updatedRestaurant.getPhoneNumber());
        existingRestaurant.setCuisineType(
                updatedRestaurant.getCuisineType());

        // Do not change the owner through this update method.
        // Add a separate, authorized operation if ownership changes
        // are needed in the future.

        return restaurantRepository.save(existingRestaurant);
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = getRestaurantById(id);
        restaurantRepository.delete(restaurant);
    }
}
