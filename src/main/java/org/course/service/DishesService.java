package org.course.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.course.dto.DishesCreateDTO;
import org.course.dto.DishesDto;
import org.course.entity.Dishes;
import org.course.mapper.DishesMapper;
import org.course.repository.DishesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DishesService {

    private static final Logger logger = LoggerFactory.getLogger(DishesService.class);
    private final DishesRepository dishesRepository;
    private final DishesMapper dishesMapper;

    @Autowired
    public DishesService(DishesRepository dishesRepository, DishesMapper dishesMapper) {
        this.dishesRepository = dishesRepository;
        this.dishesMapper = dishesMapper;
    }

    public List<DishesDto> getAllDishes()
    {
        List<DishesDto> dishes = dishesRepository.findAll().stream()
                .map(dishesMapper::toDto)
                .toList();
        return dishes;
    }


    public List<DishesDto> getDishesByCategory(String category) {
        return dishesRepository.findByCategory(category).stream()
                .map(dishesMapper::toDto)
                .toList();
    }


    public List<DishesDto> sortDishesByPrice(boolean ascending) {
        List<Dishes> dishes = ascending ? dishesRepository.findAllByOrderByPriceAsc() : dishesRepository.findAllByOrderByPriceDesc();
        return dishes.stream()
                .map(dishesMapper::toDto)
                .toList();
    }


    public List<DishesDto> sortDishesByName(boolean ascending) {
        List<Dishes> dishes = ascending ? dishesRepository.findAllByOrderByNameAsc() : dishesRepository.findAllByOrderByNameDesc();
        return dishes.stream()
                .map(dishesMapper::toDto)
                .toList();
    }

    @Cacheable(value = "dishesCache", unless = "#result == null")
    public Optional<DishesDto> getDishesById(long id) {
        logger.info("Запит на отримання страви..");
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
                .orElseThrow(() -> new RuntimeException("Dish not found"));
        dishesMapper.partialUpdate(dishesDto, dishes);
        Dishes updatedDishes = dishesRepository.save(dishes);
        return dishesMapper.toDto(updatedDishes);
    }

    public void deleteDishes(long id) {
        dishesRepository.deleteById(id);
    }
}
