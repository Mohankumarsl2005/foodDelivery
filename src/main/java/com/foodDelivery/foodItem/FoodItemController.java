
package com.foodDelivery.foodItem;

import com.foodDelivery.user.User;
import com.foodDelivery.user.UserRepository;

import jakarta.validation.Valid;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/food-items")
public class FoodItemController {

    private final FoodItemService foodItemService;
    private final UserRepository userRepository;

    public FoodItemController(
            FoodItemService foodItemService,
            UserRepository userRepository) {
        this.foodItemService = foodItemService;
        this.userRepository = userRepository;
    }

    // Get available food items for a restaurant
    @GetMapping
    public List<FoodItemResponse> getFoodItems(
            @PathVariable Long restaurantId) {

        return foodItemService.getFoodItems(restaurantId);
    }

    // Add a food item
    @PostMapping
    public FoodItemResponse createFoodItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody FoodItemRequest request,
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        return foodItemService.createFoodItem(
                restaurantId,
                request,
                currentUser
        );
    }

    // Update a food item
    @PutMapping("/{foodItemId}")
    public FoodItemResponse updateFoodItem(
            @PathVariable Long restaurantId,
            @PathVariable Long foodItemId,
            @Valid @RequestBody FoodItemRequest request,
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        return foodItemService.updateFoodItem(
                restaurantId,
                foodItemId,
                request,
                currentUser
        );
    }

    // Delete a food item
    @DeleteMapping("/{foodItemId}")
    public String deleteFoodItem(
            @PathVariable Long restaurantId,
            @PathVariable Long foodItemId,
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        foodItemService.deleteFoodItem(
                restaurantId,
                foodItemId,
                currentUser
        );

        return "Food item deleted successfully";
    }

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {

            throw new AccessDeniedException(
                    "Authentication is required"
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException(
                        "Authenticated user not found"
                ));
    }
}
