package com.foodDelivery.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrder_User_Email(String email);

    Optional<OrderItem> findByIdAndOrder_User_Email(Long id, String email);
}
