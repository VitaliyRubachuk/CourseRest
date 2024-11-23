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

import java.util.*;
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
    public ResponseEntity<List<OrderDto>> getAllOrders(@RequestParam(required = false) OrderStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        List<OrderDto> orders = orderService.getAllOrders(status, page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderDto>> getMyOrders(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        List<OrderDto> orders = orderService.getOrdersByUserEmail(email, page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{size}/page/{page}")
    public ResponseEntity<List<OrderDto>> getAllOrdersWithPath(@PathVariable int size, @PathVariable int page, @RequestParam(required = false) OrderStatus status) {
        List<OrderDto> orders = orderService.getAllOrders(status, page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my/{size}/page/{page}")
    public ResponseEntity<List<OrderDto>> getMyOrdersWithPath(@PathVariable int size, @PathVariable int page) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        List<OrderDto> orders = orderService.getOrdersByUserEmail(email, page, size);
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

    @PutMapping("/my/{id}")
    public ResponseEntity<OrderDto> updateMyOrder(
            @PathVariable long id,
            @Valid @RequestBody OrderCreateDTO orderCreateDTO,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(null);
        }

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        try {
            OrderDto updatedOrder = orderService.updateUserOrder(id, orderCreateDTO, email);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable long id, @RequestBody Map<String, String> request) {
        String statusString = request.get("status");
        try {
            OrderStatus status = OrderStatus.valueOf(statusString.toUpperCase());
            OrderDto updatedOrder = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException e) {
            String errorMessage = String.format("Статус '%s' є некоректним. Доступні значення: %s.",
                    statusString,
                    Arrays.stream(OrderStatus.values())
                            .map(Enum::name)
                            .collect(Collectors.joining(", ")));
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", errorMessage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Виникла внутрішня помилка сервера."));
        }
    }
}
