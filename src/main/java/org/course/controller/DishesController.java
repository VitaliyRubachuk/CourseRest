package org.course.controller;

import org.course.dto.DishesCreateDTO;
import org.course.dto.DishesDto;
import org.course.service.DishesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dishes")
public class DishesController {

    private final DishesService dishesService;

    @Autowired
    public DishesController(DishesService dishesService) {
        this.dishesService = dishesService;
    }

    @GetMapping
    public ResponseEntity<List<DishesDto>> getAllDishes() {
        List<DishesDto> dishes = dishesService.getAllDishes();
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DishesDto> getDishesById(@PathVariable long id) {
        Optional<DishesDto> dishes = dishesService.getDishesById(id);
        return dishes.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<DishesDto> createDishes(@RequestBody DishesCreateDTO dishesCreateDTO) {
        System.out.println("Received request to create Dishes: " + dishesCreateDTO);
        DishesDto createdDishes = dishesService.createDishes(dishesCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDishes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DishesDto> updateDishes(@PathVariable long id, @RequestBody DishesDto dishesDto) {
        DishesDto updatedDishes = dishesService.updateDishes(id, dishesDto);
        return ResponseEntity.ok(updatedDishes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDishes(@PathVariable long id) {
        dishesService.deleteDishes(id);
        return ResponseEntity.noContent().build();
    }
}
