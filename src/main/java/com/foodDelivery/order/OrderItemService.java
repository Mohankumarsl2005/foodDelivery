
package com.foodDelivery.order;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public OrderItem saveOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    public OrderItem getOrderItemById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Order item not found with id: " + id));
    }

    public void deleteOrderItem(Long id) {
        if (!orderItemRepository.existsById(id)) {
            throw new RuntimeException("Order item not found with id: " + id);
        }

        orderItemRepository.deleteById(id);
    }
}
