package org.course.controller;

import jakarta.validation.Valid;
import org.course.dto.OrderCreateDTO;
import org.course.dto.OrderDto;
import org.course.entity.OrderStatus;
import org.course.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders(
            @RequestParam(required = false) OrderStatus status) {
        List<OrderDto> orders = orderService.getAllOrders(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderDto>> getMyOrders() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        List<OrderDto> orders = orderService.getOrdersByUserEmail(email);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable long id) {
        Optional<OrderDto> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Object> createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {

            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest().body(errorMessages);
        }

        OrderDto createdOrder = orderService.createOrder(orderCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateOrder(@PathVariable long id, @Valid @RequestBody OrderCreateDTO orderCreateDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {

            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest().body(errorMessages);
        }

        OrderDto updatedOrder = orderService.updateOrder(id, orderCreateDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable long id) {
        try {
            orderService.deleteOrder(id);

            return ResponseEntity.ok("Замовлення успішно видалено.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Замовлення не знайдено.");
        }
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable long id, @RequestParam OrderStatus status) {
        OrderDto updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }
}
