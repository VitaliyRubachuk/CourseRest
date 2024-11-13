package org.course.service;

import org.course.dto.DishesCreateDTO;
import org.course.dto.DishesDto;
import org.course.entity.Dishes;
import org.course.mapper.DishesMapper;
import org.course.repository.DishesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DishesService {

    private final DishesRepository dishesRepository;
    private final DishesMapper dishesMapper;

    @Autowired
    public DishesService(DishesRepository dishesRepository, DishesMapper dishesMapper) {
        this.dishesRepository = dishesRepository;
        this.dishesMapper = dishesMapper;
    }

    public List<DishesDto> getAllDishes() {
        return dishesRepository.findAll().stream()
                .map(dishesMapper::toDto)
                .toList();
    }

    public Optional<DishesDto> getDishesById(long id) {
        return dishesRepository.findById(id)
                .map(dishesMapper::toDto);
    }

    public DishesDto createDishes(DishesCreateDTO dishesCreateDTO) {
        Dishes dishes = dishesMapper.toEntity(dishesCreateDTO);
        Dishes savedDishes = dishesRepository.save(dishes);
        return dishesMapper.toDto(savedDishes);
    }

    public DishesDto updateDishes(long id, DishesDto dishesDto) {
        Dishes dishes = dishesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dishes not found"));
        dishesMapper.partialUpdate(dishesDto, dishes);
        Dishes updatedDishes = dishesRepository.save(dishes);
        return dishesMapper.toDto(updatedDishes);
    }

    public void deleteDishes(long id) {
        dishesRepository.deleteById(id);
    }
}
