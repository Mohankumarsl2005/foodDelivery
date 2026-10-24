
package com.foodDelivery.order;

import java.util.Map;
import java.util.Set;

public final class OrderStatusTransitions {

    private OrderStatusTransitions() {
    }

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED =
            Map.of(
                    OrderStatus.PLACED,
                    Set.of(
                            OrderStatus.CONFIRMED,
                            OrderStatus.CANCELLED
                    ),

                    OrderStatus.CONFIRMED,
                    Set.of(
                            OrderStatus.PREPARING,
                            OrderStatus.CANCELLED
                    ),

                    OrderStatus.PREPARING,
                    Set.of(OrderStatus.OUT_FOR_DELIVERY),

                    OrderStatus.OUT_FOR_DELIVERY,
                    Set.of(OrderStatus.DELIVERED),

                    OrderStatus.DELIVERED,
                    Set.of(),

                    OrderStatus.CANCELLED,
                    Set.of()
            );

    public static void validate(
            OrderStatus current,
            OrderStatus next) {

        if (current == null || next == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null");
        }

        if (!ALLOWED.getOrDefault(current, Set.of()).contains(next)) {
            throw new IllegalStateException(
                    "Invalid order status transition: "
                            + current + " -> " + next);
        }
    }
}
