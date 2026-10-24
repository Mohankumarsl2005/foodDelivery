package com.foodDelivery.order;

import com.foodDelivery.foodItem.FoodItem;
import com.foodDelivery.foodItem.FoodItemRepository;
import com.foodDelivery.orderItem.OrderItem;
import com.foodDelivery.orderItem.OrderItemRepository;
import com.foodDelivery.user.User;
import com.foodDelivery.user.UserRepository;
import com.foodDelivery.restaurant.Restaurant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldThrowUnauthorized_whenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.createOrder(
                        null, "missing@example.com")
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_shouldRejectNullRequest() {
        User customer = mock(User.class);

        when(userRepository.findByEmail("customer@example.com"))
                .thenReturn(Optional.of(customer));

        when(customer.getRole())
                .thenReturn(User.Role.CUSTOMER);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.createOrder(
                        null, "customer@example.com")
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(foodItemRepository);
    }

    @Test
    void createOrder_shouldSaveOrderWithCorrectTotal() {
        // Arrange: create a mock customer.
        User customer = mock(User.class);

        when(userRepository.findByEmail("customer@example.com"))
                .thenReturn(Optional.of(customer));

        when(customer.getRole())
                .thenReturn(User.Role.CUSTOMER);

        when(customer.getId()).thenReturn(7L);

        // Arrange: create a restaurant and food item.
        Restaurant restaurant = mock(Restaurant.class);
        when(restaurant.getId()).thenReturn(1L);

        FoodItem food = mock(FoodItem.class);

        when(foodItemRepository.findById(10L))
                .thenReturn(Optional.of(food));

        when(food.getId()).thenReturn(10L);
        when(food.getName()).thenReturn("Burger");
        when(food.getPrice()).thenReturn(
                new BigDecimal("100.00"));
        when(food.isAvailable()).thenReturn(true);
        when(food.getRestaurant()).thenReturn(restaurant);

        // Arrange: two burgers at 100.00 each.
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(10L, 2))
        );

        // Simulate the database saving the order.
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(500L);
                    return order;
                });

        // Act: call the actual service method.
        OrderResponse response = orderService.createOrder(
                request, "customer@example.com");

        // Assert: check the returned response.
        assertNotNull(response);
        assertEquals(
                new BigDecimal("200.00"),
                response.totalAmount()
        );

        // Assert: check the order passed to the repository.
        var orderCaptor =
                org.mockito.ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertEquals(500L, savedOrder.getId());
        assertEquals(customer, savedOrder.getUser());
        assertEquals(
                new BigDecimal("200.00"),
                savedOrder.getTotalAmount()
        );
        assertEquals(OrderStatus.PLACED, savedOrder.getStatus());

        // Assert: the order items were saved.
        verify(orderItemRepository).saveAll(anyList());
    }
}
