package org.course.repository;

import org.course.entity.Order;
import org.course.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    ///api/orders?status=PENDING
    List<Order> findByStatus(OrderStatus status);
}
