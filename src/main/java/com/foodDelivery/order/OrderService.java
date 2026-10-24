
package com.foodDelivery.order;

import com.foodDelivery.dto.OrderItemResponse;
import com.foodDelivery.foodItem.FoodItem;
import com.foodDelivery.foodItem.FoodItemRepository;
import com.foodDelivery.orderItem.OrderItem;
import com.foodDelivery.orderItem.OrderItemRepository;
import com.foodDelivery.user.User;
import com.foodDelivery.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            FoodItemRepository foodItemRepository,
            UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.foodItemRepository = foodItemRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates an order after validating customer, food items,
     * availability, duplicate items, and restaurant consistency.
     */
    @Transactional
    public OrderResponse createOrder(
            CreateOrderRequest request,
            String authenticatedEmail) {

        User customer = userRepository
                .findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"));

        if (customer.getRole() != User.Role.CUSTOMER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only customers can create orders");
        }

        if (request == null
                || request.items() == null
                || request.items().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "An order must contain at least one item");
        }

        Set<Long> requestedIds = new HashSet<>();
        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderItemResponse> responseItems = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;

        // Local to this order creation, not a service-level field.
        Long restaurantId = null;

        for (OrderItemRequest requested : request.items()) {

            if (requested == null
                    || requested.foodItemId() == null
                    || requested.foodItemId() <= 0
                    || requested.quantity() <= 0
                    || requested.quantity() > 100) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid food item ID or quantity");
            }

            if (!requestedIds.add(requested.foodItemId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate food item IDs are not allowed");
            }

            FoodItem food = foodItemRepository
                    .findById(requested.foodItemId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Food item not found: "
                                    + requested.foodItemId()));

            if (!food.isAvailable()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Food item is unavailable: " + food.getName());
            }

            if (food.getRestaurant() == null
                    || food.getRestaurant().getId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Food item has no valid restaurant");
            }

            Long foodRestaurantId = food.getRestaurant().getId();

            // All items in one order must belong to one restaurant.
            if (restaurantId == null) {
                restaurantId = foodRestaurantId;
            } else if (!restaurantId.equals(foodRestaurantId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "All items in an order must belong "
                                + "to the same restaurant");
            }

            BigDecimal unitPrice = food.getPrice();

            if (unitPrice == null || unitPrice.signum() < 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Food item has an invalid price");
            }

            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(requested.quantity()));

            total = total.add(subtotal);

            OrderItem item = new OrderItem();
            item.setFoodItem(food);
            item.setQuantity(requested.quantity());

            // Preserve the price at the time the order is created.
            item.setPrice(unitPrice);

            orderItems.add(item);

            responseItems.add(new OrderItemResponse(
                    food.getId(),
                    food.getName(),
                    requested.quantity(),
                    unitPrice,
                    subtotal
            ));
        }

        Order order = new Order();
        order.setUser(customer);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PLACED);

        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
        }

        orderItemRepository.saveAll(orderItems);

        return new OrderResponse(
                savedOrder.getId(),
                customer.getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getCreatedAt(),
                responseItems
        );
    }

    /**
     * Adds an item only when the customer owns the order,
     * the order is editable, and the restaurant matches.
     */
    @Transactional
    public com.foodDelivery.orderItem.OrderItemResponse addOrderItem(
            com.foodDelivery.orderItem.OrderItemRequest request,
            String authenticatedEmail) {

        if (request == null
                || request.orderId() == null
                || request.orderId() <= 0
                || request.foodItemId() == null
                || request.foodItemId() <= 0
                || request.quantity() <= 0
                || request.quantity() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid order item request");
        }

        Order order = orderRepository
                .findByIdAndUser_Email(
                        request.orderId(), authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found for this user"));

        ensureOrderIsEditable(order);

        FoodItem foodItem = foodItemRepository
                .findById(request.foodItemId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Food item not found"));

        if (!foodItem.isAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Food item is unavailable");
        }

        if (foodItem.getRestaurant() == null
                || foodItem.getRestaurant().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Food item has no valid restaurant");
        }

        List<OrderItem> existingItems =
                orderItemRepository.findAllByOrder_Id(order.getId());

        Long newRestaurantId = foodItem.getRestaurant().getId();

        // Check every existing item, not just the first one.
        for (OrderItem existingItem : existingItems) {
            Long existingRestaurantId = existingItem
                    .getFoodItem()
                    .getRestaurant()
                    .getId();

            if (!newRestaurantId.equals(existingRestaurantId)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cannot add an item because the order "
                                + "already contains items from "
                                + "another restaurant");
            }
        }

        if (orderItemRepository
                .findByOrder_IdAndFoodItem_Id(
                        order.getId(), foodItem.getId())
                .isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This food item is already in the order");
        }

        BigDecimal unitPrice = foodItem.getPrice();

        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Food item has an invalid price");
        }

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setFoodItem(foodItem);
        item.setQuantity(request.quantity());
        item.setPrice(unitPrice);

        OrderItem savedItem = orderItemRepository.saveAndFlush(item);

        recalculateTotal(order);

        return new com.foodDelivery.orderItem.OrderItemResponse(
                savedItem.getId(),
                order.getId(),
                foodItem.getId(),
                foodItem.getName(),
                savedItem.getQuantity(),
                savedItem.getPrice(),
                savedItem.getPrice().multiply(
                        BigDecimal.valueOf(savedItem.getQuantity()))
        );
    }

    /**
     * Removes an item only while the order is editable.
     */
    @Transactional
    public void removeOrderItem(
            Long orderItemId,
            String authenticatedEmail) {

        OrderItem item = orderItemRepository
                .findByIdAndOrder_User_Email(
                        orderItemId, authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order item not found"));

        Order order = item.getOrder();

        ensureOrderIsEditable(order);

        orderItemRepository.delete(item);
        orderItemRepository.flush();

        recalculateTotal(order);
    }

    private void ensureOrderIsEditable(Order order) {
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order items can only be changed "
                            + "while the order is PLACED");
        }
    }

    /**
     * Recalculates the total using persisted item prices.
     */
    private void recalculateTotal(Order order) {

        BigDecimal total = orderItemRepository
                .findAllByOrder_Id(order.getId())
                .stream()
                .map(item -> item.getPrice().multiply(
                        BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);
        orderRepository.save(order);
    }
}
