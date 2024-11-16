package org.course.controller;

import jakarta.validation.Valid;
import org.course.dto.DishesCreateDTO;
import org.course.dto.DishesDto;
import org.course.service.DishesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dishes")
public class DishesController {

    private final DishesService dishesService;

    public DishesController(DishesService dishesService) {
        this.dishesService = dishesService;
    }

    @GetMapping
    public ResponseEntity<List<DishesDto>> getAllDishes() {
        List<DishesDto> dishes = dishesService.getAllDishes();
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<DishesDto>> getDishesByCategory(@PathVariable String category) {
        List<DishesDto> dishes = dishesService.getDishesByCategory(category);
        return ResponseEntity.ok(dishes);
    }

    //true / false - зростання / спадання
    @GetMapping("/sort/price/{order}")
    public ResponseEntity<List<DishesDto>> sortDishesByPrice(@PathVariable boolean order) {
        List<DishesDto> dishes = dishesService.sortDishesByPrice(order);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/sort/name/{order}")
    public ResponseEntity<List<DishesDto>> sortDishesByName(@PathVariable boolean order) {
        List<DishesDto> dishes = dishesService.sortDishesByName(order);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DishesDto> getDishesById(@PathVariable long id) {
        return dishesService.getDishesById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DishesDto> createDishes(@Valid  @RequestBody DishesCreateDTO dishesCreateDTO) {
        DishesDto createdDishes = dishesService.createDishes(dishesCreateDTO);
        return ResponseEntity.status(201).body(createdDishes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DishesDto> updateDishes(@PathVariable long id,@Valid @RequestBody DishesDto dishesDto) {
        DishesDto updatedDishes = dishesService.updateDishes(id, dishesDto);
        return ResponseEntity.ok(updatedDishes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDishes(@PathVariable long id) {
        dishesService.deleteDishes(id);
        return ResponseEntity.noContent().build();
    }
}
