
package com.foodDelivery.foodItem;

import com.foodDelivery.restaurant.Restaurant;
import com.foodDelivery.restaurant.RestaurantRepository;
import com.foodDelivery.user.User;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;
    private final RestaurantRepository restaurantRepository;

    public FoodItemService(
            FoodItemRepository foodItemRepository,
            RestaurantRepository restaurantRepository) {
        this.foodItemRepository = foodItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional(readOnly = true)
    public List<FoodItemResponse> getFoodItems(Long restaurantId) {

        // Confirm that the restaurant exists.
        getRestaurant(restaurantId);

        return foodItemRepository
                .findByRestaurant_IdAndAvailableTrueOrderByNameAsc(
                        restaurantId
                )
                .stream()
                .map(FoodItemResponse::from)
                .toList();
    }

    @Transactional
    public FoodItemResponse createFoodItem(
            Long restaurantId,
            FoodItemRequest request,
            User currentUser) {

        validateRequest(request);
        Restaurant restaurant = getRestaurant(restaurantId);
        checkPermission(restaurant, currentUser);

        FoodItem foodItem = new FoodItem();
        foodItem.setRestaurant(restaurant);
        foodItem.setName(request.getName().trim());
        foodItem.setDescription(request.getDescription());
        foodItem.setPrice(request.getPrice());
        foodItem.setAvailable(request.isAvailable());

        FoodItem saved = foodItemRepository.save(foodItem);

        return FoodItemResponse.from(saved);
    }

    @Transactional
    public FoodItemResponse updateFoodItem(
            Long restaurantId,
            Long foodItemId,
            FoodItemRequest request,
            User currentUser) {

        validateRequest(request);

        Restaurant restaurant = getRestaurant(restaurantId);
        checkPermission(restaurant, currentUser);

        FoodItem foodItem = getFoodItem(foodItemId);

        if (!foodItem.getRestaurant().getId().equals(restaurantId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Food item not found in this restaurant"
            );
        }

        foodItem.setName(request.getName().trim());
        foodItem.setDescription(request.getDescription());
        foodItem.setPrice(request.getPrice());
        foodItem.setAvailable(request.isAvailable());

        return FoodItemResponse.from(
                foodItemRepository.save(foodItem)
        );
    }

    @Transactional
    public void deleteFoodItem(
            Long restaurantId,
            Long foodItemId,
            User currentUser) {

        Restaurant restaurant = getRestaurant(restaurantId);
        checkPermission(restaurant, currentUser);

        FoodItem foodItem = getFoodItem(foodItemId);

        if (!foodItem.getRestaurant().getId().equals(restaurantId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Food item not found in this restaurant"
            );
        }

        foodItemRepository.delete(foodItem);
    }

    private Restaurant getRestaurant(Long restaurantId) {
        if (restaurantId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Restaurant ID is required"
            );
        }

        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Restaurant not found"
                ));
    }

    private FoodItem getFoodItem(Long foodItemId) {
        if (foodItemId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Food item ID is required"
            );
        }

        return foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Food item not found"
                ));
    }

    private void validateRequest(FoodItemRequest request) {
        if (request == null
                || request.getName() == null
                || request.getName().isBlank()
                || request.getPrice() == null
                || request.getPrice().signum() <= 0
                || request.getName().length() > 100
                || (request.getDescription() != null
                && request.getDescription().length() > 255)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid food item details"
            );
        }
    }

    private void checkPermission(
            Restaurant restaurant,
            User currentUser) {

        if (currentUser == null || currentUser.getId() == null) {
            throw new AccessDeniedException(
                    "Authentication is required"
            );
        }

        User.Role role = currentUser.getRole();

        boolean isAdmin = role == User.Role.ADMIN;

        boolean isOwner = role == User.Role.RESTAURANT_OWNER
                && restaurant.getOwner().getId()
                .equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "You are not allowed to manage this restaurant's menu"
            );
        }
    }
}
