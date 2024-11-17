package org.course.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.course.dto.OrderCreateDTO;
import org.course.dto.OrderDto;
import org.course.entity.Dishes;
import org.course.entity.Order;
import org.course.entity.User;
import org.course.entity.OrderStatus;
import org.course.mapper.OrderMapper;
import org.course.repository.DishesRepository;
import org.course.repository.OrderRepository;
import org.course.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(DishesService.class);
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final DishesRepository dishesRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper,
                        UserRepository userRepository, DishesRepository dishesRepository) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.userRepository = userRepository;
        this.dishesRepository = dishesRepository;
    }

    public List<OrderDto> getAllOrders(OrderStatus status) {
        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatus(status);
        } else {
            orders = orderRepository.findAll();
        }
        return orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "orderCache", unless = "#result == null")
    public Optional<OrderDto> getOrderById(long id) {
        logger.info("Запит на отримання замовлення..");
        return orderRepository.findById(id)
                .map(orderMapper::toDto);
    }

    public List<OrderDto> getOrdersByUserEmail(String email) {
        List<Order> orders = orderRepository.findByUserEmail(email);

        return orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }


    public OrderDto createOrder(OrderCreateDTO orderCreateDTO) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));


        List<Dishes> dishes = dishesRepository.findAllById(orderCreateDTO.dishIds());

        List<Dishes> orderedDishes = orderCreateDTO.dishIds().stream()
                .map(dishId -> dishes.stream()
                        .filter(dish -> dish.getId() == dishId)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Dish not found")))
                .collect(Collectors.toList());

        double totalPrice = orderedDishes.stream()
                .mapToDouble(Dishes::getPrice)
                .sum();

        Order order = new Order();
        order.setUser(user);
        order.setAddition(orderCreateDTO.addition());
        order.setDishes(orderedDishes);
        order.setFullprice(totalPrice);
        order.updateDishIdsString();

        if (orderCreateDTO.status() != null) {
            order.setStatus(OrderStatus.valueOf(orderCreateDTO.status()));
        } else {
            order.setStatus(OrderStatus.PENDING);
        }

        Order savedOrder = orderRepository.save(order);
        orderRepository.flush();

        return orderMapper.toDto(savedOrder);
    }


    public OrderDto updateOrder(long id, OrderCreateDTO orderCreateDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setAddition(orderCreateDTO.addition());

        List<Dishes> dishes = dishesRepository.findAllById(orderCreateDTO.dishIds());

        List<Dishes> updatedDishes = new ArrayList<>();
        for (Long dishId : orderCreateDTO.dishIds()) {
            Dishes dish = dishes.stream()
                    .filter(d -> d.getId() == dishId)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Dish not found"));
            updatedDishes.add(dish);
        }

        order.setDishes(updatedDishes);

        double updatedTotalPrice = updatedDishes.stream()
                .mapToDouble(Dishes::getPrice)
                .sum();
        order.setFullprice(updatedTotalPrice);

        order.updateDishIdsString();

        if (orderCreateDTO.status() != null) {
            order.setStatus(OrderStatus.valueOf(orderCreateDTO.status()));
        }

        order = orderRepository.save(order);

        return orderMapper.toDto(order);
    }


    public OrderDto updateOrderStatus(long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return new OrderDto(
                updatedOrder.getId(),
                updatedOrder.getUser().getId(),
                updatedOrder.getDishes().stream().map(Dishes::getId).collect(Collectors.toList()),
                updatedOrder.getFullprice(),
                updatedOrder.getAddition(),
                updatedOrder.getStatus()
        );
    }


    public void deleteOrder(long id) {
    orderRepository.deleteById(id);
}

}