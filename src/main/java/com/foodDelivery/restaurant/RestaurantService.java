
package com.foodDelivery.restaurant;

import java.util.List;

import com.foodDelivery.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(
            RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public RestaurantResponse createRestaurant(
            RestaurantRequest request,
            User authenticatedUser) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Restaurant request is required");
        }

        validateName(request.getName());
        checkAuthenticatedUser(authenticatedUser);

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName().trim());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhoneNumber(request.getPhoneNumber());
        restaurant.setCuisineType(request.getCuisineType());

        // Assign the owner from the authenticated user.
        restaurant.setOwner(authenticatedUser);

        // New restaurants start closed.
        restaurant.setOpen(false);

        Restaurant saved = restaurantRepository.save(restaurant);
        return RestaurantResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(RestaurantResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = getRestaurantEntityById(id);
        return RestaurantResponse.from(restaurant);
    }

    @Transactional
    public RestaurantResponse updateRestaurant(
            Long id,
            RestaurantRequest request,
            Long currentUserId) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Restaurant request is required");
        }

        validateName(request.getName());

        Restaurant existing = getRestaurantEntityById(id);
        checkOwnership(existing, currentUserId);

        existing.setName(request.getName().trim());
        existing.setDescription(request.getDescription());
        existing.setAddress(request.getAddress());
        existing.setPhoneNumber(request.getPhoneNumber());
        existing.setCuisineType(request.getCuisineType());

        // Owner and opening status cannot be changed here.
        Restaurant saved = restaurantRepository.save(existing);
        return RestaurantResponse.from(saved);
    }

    @Transactional
    public void deleteRestaurant(Long id, Long currentUserId) {
        Restaurant restaurant = getRestaurantEntityById(id);
        checkOwnership(restaurant, currentUserId);
        restaurantRepository.delete(restaurant);
    }

    private Restaurant getRestaurantEntityById(Long id) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Restaurant ID is required");
        }

        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Restaurant name is required");
        }
    }

    private void checkAuthenticatedUser(User user) {
        if (user == null || user.getId() == null) {
            throw new AccessDeniedException(
                    "Authentication is required");
        }
    }

    private void checkOwnership(
            Restaurant restaurant,
            Long currentUserId) {

        if (currentUserId == null
                || restaurant.getOwner() == null
                || restaurant.getOwner().getId() == null
                || !currentUserId.equals(
                restaurant.getOwner().getId())) {
            throw new AccessDeniedException(
                    "You do not own this restaurant");
        }
    }
}
