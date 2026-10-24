
package com.foodDelivery.restaurant;

import java.util.List;

import com.foodDelivery.user.User;
import com.foodDelivery.user.UserRepository;

import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final UserRepository userRepository;

    public RestaurantController(
            RestaurantService restaurantService,
            UserRepository userRepository) {
        this.restaurantService = restaurantService;
        this.userRepository = userRepository;
    }

    // Get all restaurants
    @GetMapping
    public List<RestaurantResponse> getRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    // Get restaurant by ID
    @GetMapping("/{id}")
    public RestaurantResponse getRestaurantById(
            @PathVariable Long id) {
        return restaurantService.getRestaurantById(id);
    }

    // Create restaurant
    @PostMapping
    public RestaurantResponse createRestaurant(
            @Valid @RequestBody RestaurantRequest request,
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        return restaurantService.createRestaurant(
                request, currentUser);
    }

    // Update restaurant
    @PutMapping("/{id}")
    public RestaurantResponse updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request,
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        return restaurantService.updateRestaurant(
                id, request, currentUser);
    }

    // Delete restaurant
    @DeleteMapping("/{id}")
    public String deleteRestaurant(
            @PathVariable Long id,
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        restaurantService.deleteRestaurant(
                id, currentUser);

        return "Restaurant deleted successfully";
    }

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new AccessDeniedException(
                    "Authentication is required");
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Authenticated user not found"));
    }
}
