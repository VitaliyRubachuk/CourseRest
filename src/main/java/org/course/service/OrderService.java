package org.course.service;

import org.course.dto.OrderCreateDTO;
import org.course.dto.OrderDto;
import org.course.entity.Dishes;
import org.course.entity.Order;
import org.course.entity.User;
import org.course.mapper.OrderMapper;
import org.course.repository.DishesRepository;
import org.course.repository.OrderRepository;
import org.course.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

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

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<OrderDto> getOrderById(long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDto);
    }

    public OrderDto createOrder(OrderCreateDTO orderCreateDTO) {
        User user = userRepository.findById(orderCreateDTO.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Dishes> dishes = dishesRepository.findAllById(orderCreateDTO.dishIds());
        Order order = new Order();
        order.setUser(user);
        order.setDishes(dishes);
        order.setFullprice(orderCreateDTO.fullprice());
        order.setAddition(orderCreateDTO.addition());

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    public OrderDto updateOrder(long id, OrderCreateDTO orderCreateDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setFullprice(orderCreateDTO.fullprice());
        order.setAddition(orderCreateDTO.addition());

        List<Dishes> dishes = dishesRepository.findAllById(orderCreateDTO.dishIds());
        order.setDishes(dishes);

        order = orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    public void deleteOrder(long id) {
        orderRepository.deleteById(id);
    }
}
