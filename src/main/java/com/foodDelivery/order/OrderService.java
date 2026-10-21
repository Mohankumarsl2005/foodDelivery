
package com.foodDelivery.order;

import com.foodDelivery.dto.CreateOrderRequest;
import com.foodDelivery.dto.OrderItemRequest;
import com.foodDelivery.dto.OrderItemResponse;
import com.foodDelivery.dto.OrderResponse;
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

        Set<Long> requestedIds = new HashSet<>();
        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderItemResponse> responseItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest requested : request.items()) {

            if (!requestedIds.add(requested.foodItemId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate food item IDs are not allowed");
            }

            FoodItem food = foodItemRepository
                    .findById(requested.foodItemId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Food item not found: " + requested.foodItemId()));

            if (!food.isAvailable()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Food item is unavailable: " + food.getName());
            }

            BigDecimal unitPrice = food.getPrice();
            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(requested.quantity()));

            total = total.add(subtotal);

            OrderItem item = new OrderItem();
            item.setFoodItem(food);
            item.setQuantity(requested.quantity());
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

        // Save the parent first to generate its ID.
        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
        }

        orderItemRepository.saveAll(orderItems);

        // Both saves participate in the same transaction.
        return new OrderResponse(
                savedOrder.getId(),
                customer.getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getCreatedAt(),
                responseItems
        );
    }
}
