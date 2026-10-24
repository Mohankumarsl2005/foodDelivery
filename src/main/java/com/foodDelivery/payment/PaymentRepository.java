package com.foodDelivery.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder_Id(Long orderId);

    // Scope payment lookups to the owner of the associated order.
    Optional<Payment> findByIdAndOrder_User_Email(Long paymentId, String email);

    Optional<Payment> findByOrder_IdAndOrder_User_Email(Long orderId, String email);
}
