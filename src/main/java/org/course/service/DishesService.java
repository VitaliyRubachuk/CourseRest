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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.stream.Collectors;
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

    public List<DishesDto> getAllDishes() {
        logger.info("Отримання всіх страв з бази даних");
        List<DishesDto> dishes = dishesRepository.findAll().stream()
                .map(dishesMapper::toDto)
                .toList();
        logger.info("Знайдено {} страв", dishes.size());
        return dishes;
    }

    public List<DishesDto> getDishesWithPagination(int size, int page) {
        logger.info("Отримання страв з пагінацією: сторінка {}, розмір {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Dishes> dishesPage = dishesRepository.findAll(pageable);
        if (dishesPage.isEmpty()) {
            logger.warn("Не знайдено страв на сторінці {} з розміром {}", page, size);
        }
        return dishesPage.stream()
                .map(dishesMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<DishesDto> getDishesByCategory(String category) {
        logger.info("Отримання страв за категорією: {}", category);
        List<DishesDto> dishes = dishesRepository.findByCategory(category).stream()
                .map(dishesMapper::toDto)
                .toList();
        if (dishes.isEmpty()) {
            logger.warn("Не знайдено жодної страви для категорії: {}", category);
        }
        return dishes;
    }

    public List<DishesDto> sortDishesByPrice(boolean ascending) {
        logger.info("Сортування страв за ціною в порядку {}", ascending ? "зростання" : "спадання");
        List<Dishes> dishes = ascending ? dishesRepository.findAllByOrderByPriceAsc() : dishesRepository.findAllByOrderByPriceDesc();
        if (dishes.isEmpty()) {
            logger.warn("Жодних страв для сортування за ціною");
        }
        return dishes.stream()
                .map(dishesMapper::toDto)
                .toList();
    }

    public List<DishesDto> sortDishesByName(boolean ascending) {
        logger.info("Сортування страв за назвою в порядку {}", ascending ? "зростання" : "спадання");
        List<Dishes> dishes = ascending ? dishesRepository.findAllByOrderByNameAsc() : dishesRepository.findAllByOrderByNameDesc();
        if (dishes.isEmpty()) {
            logger.warn("Жодних страв для сортування за назвою");
        }
        return dishes.stream()
                .map(dishesMapper::toDto)
                .toList();
    }

    @Cacheable(value = "dishesCache", unless = "#result == null")
    public Optional<DishesDto> getDishesById(long id) {
        logger.info("Запит на отримання страви з ID: {}", id);
        Optional<DishesDto> dish = dishesRepository.findById(id)
                .map(dishesMapper::toDto);
        if (dish.isEmpty()) {
            logger.warn("Страва з ID: {} не знайдена", id);
        } else {
            logger.info("Страва з ID: {} знайдена", id);
        }
        return dish;
    }

    public DishesDto createDishes(DishesCreateDTO dishesCreateDTO) {
        logger.info("Створення нової страви з даними: {}", dishesCreateDTO);
        Dishes dishes = dishesMapper.toEntity(dishesCreateDTO);
        Dishes savedDishes = dishesRepository.save(dishes);
        logger.info("Страва створена з ID: {}", savedDishes.getId());
        return dishesMapper.toDto(savedDishes);
    }

    public DishesDto updateDishes(long id, DishesDto dishesDto) {
        logger.info("Оновлення страви з ID: {}", id);
        Dishes dishes = dishesRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Страва з ID: {} не знайдена", id);
                    return new RuntimeException("Страва не знайдена");
                });
        dishesMapper.partialUpdate(dishesDto, dishes);
        Dishes updatedDishes = dishesRepository.save(dishes);
        logger.info("Страва з ID: {} успішно оновлена", id);
        return dishesMapper.toDto(updatedDishes);
    }

    public void deleteDishes(long id) {
        logger.info("Видалення страви з ID: {}", id);
        try {
            dishesRepository.deleteById(id);
            logger.info("Страва з ID: {} успішно видалена", id);
        } catch (Exception e) {
            logger.error("Помилка при видаленні страви з ID: {}", id, e);
            throw e;
        }
    }
}
