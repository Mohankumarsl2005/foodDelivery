package com.foodDelivery.payment;

import com.foodDelivery.order.Order;
import com.foodDelivery.order.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

        @Transactional
        public Payment createPayment(
                Long orderId,
                PaymentMethod paymentMethod) {

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Order not found: " + orderId));

            if (paymentRepository.findByOrder_Id(orderId).isPresent()) {
                throw new RuntimeException(
                        "Payment already exists for order: " + orderId);
            }

            Payment payment = new Payment();
            payment.setOrder(order);

            // Replace getTotalAmount() with the actual
            // total-amount getter in your Order entity.
            BigDecimal amount = order.getTotalAmount();

            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            payment.setStatus(PaymentStatus.PENDING);

            return paymentRepository.save(payment);
        }

        public Payment getPaymentById(Long paymentId) {
            return paymentRepository.findById(paymentId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Payment not found: " + paymentId));
        }

        public Payment getPaymentByOrderId(Long orderId) {
            return paymentRepository.findByOrder_Id(orderId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Payment not found for order: " + orderId));
        }
}

