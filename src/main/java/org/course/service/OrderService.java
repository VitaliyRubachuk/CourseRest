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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
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
    @Cacheable(value = "orderCache", key = "#status != null ? #status.name().concat(':').concat(#page).concat(':').concat(#size) : 'all:'.concat(#page).concat(':').concat(#size)")
    public List<OrderDto> getAllOrders(OrderStatus status, int page, int size) {
        try {
            logger.info("Отримання всіх замовлень із статусом {}, сторінка {}, розмір {}", status, page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> ordersPage;

            if (status != null) {
                ordersPage = orderRepository.findByStatus(status, pageable);
                if (ordersPage.isEmpty()) {
                    logger.warn("Жодного замовлення зі статусом {} не знайдено", status);
                }
            } else {
                ordersPage = orderRepository.findAll(pageable);
                if (ordersPage.isEmpty()) {
                    logger.warn("Жодного замовлення не знайдено");
                }
            }

            return ordersPage.getContent().stream()
                    .map(orderMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Помилка при отриманні замовлень", e);
            throw new RuntimeException("Помилка при отриманні замовлень", e);
        }
    }

    @Cacheable(value = "orderCache", key = "#id", unless = "#result == null")
    public Optional<OrderDto> getOrderById(long id) {
        try {
            logger.info("Запит на отримання замовлення з ID: {}", id);
            Optional<OrderDto> order = orderRepository.findById(id)
                    .map(orderMapper::toDto);
            if (order.isEmpty()) {
                logger.warn("Замовлення з ID: {} не знайдено", id);
            } else {
                logger.info("Замовлення з ID: {} успішно знайдено", id);
            }
            return order;
        } catch (Exception e) {
            logger.error("Помилка при отриманні замовлення з ID: {}", id, e);
            throw new RuntimeException("Помилка при отриманні замовлення", e);
        }
    }
    @Cacheable(value = "orderCache", key = "'user:'.concat(#email).concat(':').concat(#page).concat(':').concat(#size)", unless = "#result == null || #result.isEmpty()")
    public List<OrderDto> getOrdersByUserEmail(String email, int page, int size) {
        try {
            logger.info("Отримання замовлень для користувача з email: {}, сторінка {}, розмір {}", email, page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> ordersPage = orderRepository.findByUserEmail(email, pageable);

            if (ordersPage.isEmpty()) {
                logger.warn("Жодного замовлення для користувача з email: {} не знайдено", email);
            }

            return ordersPage.getContent().stream()
                    .map(orderMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Помилка при отриманні замовлень для користувача з email: {}", email, e);
            throw new RuntimeException("Помилка при отриманні замовлень для користувача", e);
        }
    }

    @CacheEvict(value = "orderCache", allEntries = true)
    public OrderDto createOrder(OrderCreateDTO orderCreateDTO) {
        try {
            logger.info("Створення нового замовлення");

            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> {
                        logger.error("Користувача з email {} не знайдено", userDetails.getUsername());
                        return new RuntimeException("User not found");
                    });

            List<Dishes> dishes = dishesRepository.findAllById(orderCreateDTO.dishIds());
            if (dishes.isEmpty()) {
                logger.error("Страви для замовлення не знайдено");
                throw new RuntimeException("No dishes found for order");
            }

            logger.info("Отримано страви для замовлення: {} позицій", dishes.size());

            List<Dishes> orderedDishes = orderCreateDTO.dishIds().stream()
                    .map(dishId -> dishes.stream()
                            .filter(dish -> dish.getId() == dishId)
                            .findFirst()
                            .orElseThrow(() -> {
                                logger.error("Страву з ID: {} не знайдено", dishId);
                                return new RuntimeException("Dish not found");
                            }))
                    .collect(Collectors.toList());

            double totalPrice = orderedDishes.stream()
                    .mapToDouble(Dishes::getPrice)
                    .sum();
            logger.info("Загальна ціна замовлення: {}", totalPrice);

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
                logger.info("Статус замовлення встановлено за замовчуванням: PENDING");
            }

            Order savedOrder = orderRepository.save(order);
            logger.info("Замовлення успішно створено з ID: {}", savedOrder.getId());

            return orderMapper.toDto(savedOrder);
        } catch (Exception e) {
            logger.error("Помилка при створенні замовлення", e);
            throw new RuntimeException("Помилка при створенні замовлення", e);
        }
    }
    @CacheEvict(value = "orderCache", allEntries = true)
    public OrderDto updateOrder(long id, OrderCreateDTO orderCreateDTO) {
        try {
            logger.info("Оновлення замовлення з ID: {}", id);
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Замовлення з ID: {} не знайдено", id);
                        return new RuntimeException("Order not found");
                    });

            order.setAddition(orderCreateDTO.addition());
            logger.info("Додаткова інформація оновлена");

            List<Dishes> dishes = dishesRepository.findAllById(orderCreateDTO.dishIds());
            if (dishes.isEmpty()) {
                logger.error("Страви для оновлення замовлення не знайдено");
                throw new RuntimeException("No dishes found for update");
            }

            List<Dishes> updatedDishes = new ArrayList<>();
            for (Long dishId : orderCreateDTO.dishIds()) {
                Dishes dish = dishes.stream()
                        .filter(d -> d.getId() == dishId)
                        .findFirst()
                        .orElseThrow(() -> {
                            logger.error("Страву з ID: {} не знайдено", dishId);
                            return new RuntimeException("Dish not found");
                        });
                updatedDishes.add(dish);
            }
            order.setDishes(updatedDishes);
            logger.info("Список страв замовлення оновлено");

            double updatedTotalPrice = updatedDishes.stream()
                    .mapToDouble(Dishes::getPrice)
                    .sum();
            order.setFullprice(updatedTotalPrice);
            logger.info("Загальна ціна замовлення оновлена: {}", updatedTotalPrice);

            order.updateDishIdsString();
            if (orderCreateDTO.status() != null) {
                order.setStatus(OrderStatus.valueOf(orderCreateDTO.status()));
                logger.info("Статус замовлення оновлено до: {}", orderCreateDTO.status());
            }

            order = orderRepository.save(order);
            logger.info("Замовлення з ID: {} успішно оновлено", id);

            return orderMapper.toDto(order);
        } catch (Exception e) {
            logger.error("Помилка при оновленні замовлення з ID: {}", id, e);
            throw new RuntimeException("Помилка при оновленні замовлення", e);
        }
    }
    @CacheEvict(value = "orderCache", key = "#orderId")
    public OrderDto updateOrderStatus(long orderId, OrderStatus status) {
        try {
            logger.info("Оновлення статусу замовлення з ID: {} до {}", orderId, status);

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        logger.error("Замовлення з ID: {} не знайдено", orderId);
                        return new RuntimeException("Замовлення не знайдено");
                    });
            order.setStatus(status);

            Order updatedOrder = orderRepository.save(order);
            logger.info("Статус замовлення з ID: {} успішно оновлено до {}", orderId, status);

            return new OrderDto(
                    updatedOrder.getId(),
                    updatedOrder.getUser().getId(),
                    updatedOrder.getDishes().stream().map(Dishes::getId).collect(Collectors.toList()),
                    updatedOrder.getFullprice(),
                    updatedOrder.getAddition(),
                    updatedOrder.getStatus()
            );
        } catch (RuntimeException e) {
            logger.error("Помилка при оновленні статусу замовлення з ID: {}", orderId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Несподівана помилка при оновленні статусу замовлення з ID: {}", orderId, e);
            throw new RuntimeException("Несподівана помилка при оновленні статусу замовлення", e);
        }
    }

    @CacheEvict(value = "orderCache", allEntries = true)
    public OrderDto updateUserOrder(long id, OrderCreateDTO orderCreateDTO, String email) {
        try {
            logger.info("Оновлення замовлення з ID: {} користувачем із email: {}", id, email);

            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Замовлення з ID: {} не знайдено", id);
                        return new RuntimeException("Order not found");
                    });

            if (!order.getUser().getEmail().equals(email)) {
                logger.error("Користувач з email: {} не має доступу до замовлення з ID: {}", email, id);
                throw new RuntimeException("Access denied");
            }

            order.setAddition(orderCreateDTO.addition());
            logger.info("Додаткова інформація оновлена");

            List<Dishes> dishes = dishesRepository.findAllById(orderCreateDTO.dishIds());
            if (dishes.isEmpty()) {
                logger.error("Страви для оновлення замовлення не знайдено");
                throw new RuntimeException("No dishes found for update");
            }

            List<Dishes> updatedDishes = new ArrayList<>();
            for (Long dishId : orderCreateDTO.dishIds()) {
                Dishes dish = dishes.stream()
                        .filter(d -> d.getId() == dishId)
                        .findFirst()
                        .orElseThrow(() -> {
                            logger.error("Страву з ID: {} не знайдено", dishId);
                            return new RuntimeException("Dish not found");
                        });
                updatedDishes.add(dish);
            }
            order.setDishes(updatedDishes);
            logger.info("Список страв замовлення оновлено");

            double updatedTotalPrice = updatedDishes.stream()
                    .mapToDouble(Dishes::getPrice)
                    .sum();
            order.setFullprice(updatedTotalPrice);
            logger.info("Загальна ціна замовлення оновлена: {}", updatedTotalPrice);

            order.updateDishIdsString();
            if (orderCreateDTO.status() != null) {
                order.setStatus(OrderStatus.valueOf(orderCreateDTO.status()));
                logger.info("Статус замовлення оновлено до: {}", orderCreateDTO.status());
            }

            order = orderRepository.save(order);
            logger.info("Замовлення з ID: {} успішно оновлено користувачем із email: {}", id, email);

            return orderMapper.toDto(order);
        } catch (Exception e) {
            logger.error("Помилка при оновленні замовлення з ID: {} користувачем із email: {}", id, email, e);
            throw new RuntimeException("Помилка при оновленні замовлення", e);
        }
    }

    @CacheEvict(value = "orderCache", allEntries = true)
    public void deleteOrder(long id) {
        try {
            logger.info("Видалення замовлення з ID: {}", id);
            orderRepository.deleteById(id);
            logger.info("Замовлення з ID: {} успішно видалено", id);
        } catch (Exception e) {
            logger.error("Помилка при видаленні замовлення з ID: {}", id, e);
            throw new RuntimeException("Помилка при видаленні замовлення", e);
        }
    }
}
