
package com.foodDelivery.order;

import com.foodDelivery.dto.CreateOrderRequest;
import com.foodDelivery.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Principal principal) {

        OrderResponse response = orderService.createOrder(
                request,
                principal.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
