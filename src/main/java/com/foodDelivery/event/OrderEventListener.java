package com.foodDelivery.order.event;

import com.foodDelivery.event.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderEventListener {


    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventListener(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOrderCreated(OrderCreatedEvent event) {

        kafkaTemplate.send(
                "order-created",
                event.orderId().toString(),
                event
        );
    }
}