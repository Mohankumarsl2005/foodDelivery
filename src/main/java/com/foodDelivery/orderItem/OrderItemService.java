package com.foodDelivery.orderItem;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
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
