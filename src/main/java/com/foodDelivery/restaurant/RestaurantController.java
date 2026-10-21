
package com.foodDelivery.restaurant;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    // Get all restaurants
    @GetMapping
    public List<Restaurant> getRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    // Get restaurant by ID
    @GetMapping("/{id}")
    public Restaurant getRestaurantById(@PathVariable Long id) {
        return restaurantService.getRestaurantById(id);
    }

    // Create restaurant
    @PostMapping
    public Restaurant createRestaurant(
            @RequestBody Restaurant restaurant) {
        return restaurantService.createRestaurant(restaurant);
    }

    // Update restaurant
    @PutMapping("/{id}")
    public Restaurant updateRestaurant(
            @PathVariable Long id,
            @RequestBody Restaurant restaurant) {
        return restaurantService.updateRestaurant(id, restaurant);
    }

    // Delete restaurant
    @DeleteMapping("/{id}")
    public String deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return "Restaurant deleted successfully";
    }
}
