package org.course.repository;

import org.course.entity.Order;
import org.course.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    ///api/orders?status=PENDING
    List<Order> findByStatus(OrderStatus status);
    @Query("SELECT o FROM Order o WHERE o.user.email = :email")
    List<Order> findByUserEmail(String email);
}
