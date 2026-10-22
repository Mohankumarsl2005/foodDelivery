
package com.foodDelivery.order;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final FoodItemRepository foodItemRepository;

    public OrderItemService(
            OrderItemRepository orderItemRepository,
            OrderRepository orderRepository,
            FoodItemRepository foodItemRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.foodItemRepository = foodItemRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> getAllOrderItemsForUser(String email) {
        return orderItemRepository.findAllByOrder_User_Email(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderItemResponse getOrderItemForUser(Long id, String email) {
        OrderItem item = orderItemRepository
                .findByIdAndOrder_User_Email(id, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order item not found"));

        return toResponse(item);
    }

    @Transactional
    public OrderItemResponse createOrderItem(
            OrderItemRequest request, String email) {

        Order order = orderRepository
                .findByIdAndUser_Email(request.orderId(), email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found for this user"));

        FoodItem foodItem = foodItemRepository
                .findById(request.foodItemId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Food item not found"));

        if (!foodItem.isAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Food item is unavailable");
        }

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setFoodItem(foodItem);
        item.setQuantity(request.quantity());

        // Never accept the price from the client.
        item.setPrice(foodItem.getPrice());

        OrderItem saved = orderItemRepository.save(item);
        return toResponse(saved);
    }

    @Transactional
    public void deleteOrderItemForUser(Long id, String email) {
        OrderItem item = orderItemRepository
                .findByIdAndOrder_User_Email(id, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order item not found"));

        orderItemRepository.delete(item);
    }

    private OrderItemResponse toResponse(OrderItem item) {
        BigDecimal subtotal = item.getPrice().multiply(
                BigDecimal.valueOf(item.getQuantity()));

        return new OrderItemResponse(
                item.getId(),
                item.getOrder().getId(),
                item.getFoodItem().getId(),
                item.getFoodItem().getName(),
                item.getQuantity(),
                item.getPrice(),
                subtotal
        );
    }
}
