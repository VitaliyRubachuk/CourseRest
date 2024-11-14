package org.course.repository;

import org.course.entity.Dishes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishesRepository extends JpaRepository<Dishes, Long> {

    List<Dishes> findByCategory(String category);

    List<Dishes> findAllByOrderByPriceAsc();
    List<Dishes> findAllByOrderByPriceDesc();

    List<Dishes> findAllByOrderByNameAsc();
    List<Dishes> findAllByOrderByNameDesc();


}
