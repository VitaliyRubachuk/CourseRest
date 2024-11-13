package org.course.repository;

import org.course.entity.Dishes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishesRepository extends JpaRepository<Dishes, Long>
{
}
