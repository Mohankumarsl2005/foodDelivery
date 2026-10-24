package com.foodDelivery.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<Payment> createPayment(
            @PathVariable Long orderId,
            @RequestParam PaymentMethod paymentMethod,
            Authentication authentication) {

        Payment payment = paymentService.createPayment(
                orderId, paymentMethod, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(
            @PathVariable Long paymentId,
            Authentication authentication) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(paymentId, authentication.getName()));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(
            @PathVariable Long orderId,
            Authentication authentication) {

        return ResponseEntity.ok(
                paymentService.getPaymentByOrderId(orderId, authentication.getName()));
    }
}
