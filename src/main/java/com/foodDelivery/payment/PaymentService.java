package com.foodDelivery.payment;

import com.foodDelivery.order.Order;
import com.foodDelivery.order.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public Payment createPayment(Long orderId, PaymentMethod paymentMethod, String authenticatedEmail) {
        // The query checks both the order ID and its owner. A user cannot create
        // a payment for an order belonging to someone else.
        Order order = orderRepository.findByIdAndUser_Email(orderId, authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        if (paymentRepository.findByOrder_Id(orderId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A payment already exists for this order");
        }

        if (paymentMethod == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Payment method is required");
        }

        BigDecimal amount = order.getTotalAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Order total must be greater than zero");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.PENDING);

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long paymentId, String authenticatedEmail) {
        return paymentRepository.findByIdAndOrder_User_Email(paymentId, authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId, String authenticatedEmail) {
        return paymentRepository.findByOrder_IdAndOrder_User_Email(orderId, authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));
    }
}
