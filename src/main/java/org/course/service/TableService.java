package org.course.service;

import org.course.dto.OrderDto;
import org.course.entity.Tables;
import org.course.dto.TableCreateDto;
import org.course.dto.TableDto;
import org.course.mapper.TableMapper;
import org.course.repository.TableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.course.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.course.entity.User;
import org.course.exception.TableNotFoundException;
import org.course.exception.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TableService {

    private final UserRepository userRepository;
    private static final int MAX_SEATS = 30;
    private static final Logger logger = LoggerFactory.getLogger(TableService.class);
    private final TableRepository tableRepository;
    private final TableMapper tableMapper;

    @Autowired
    public TableService(TableRepository tableRepository, TableMapper tableMapper, UserRepository userRepository) {
        this.tableRepository = tableRepository;
        this.tableMapper = tableMapper;
        this.userRepository = userRepository;
    }

    public List<TableDto> getAllTables() {
        logger.info("Отримання списку всіх столиків...");
        List<TableDto> tables = tableRepository.findAll().stream()
                .map(tableMapper::toDto)
                .toList();
        logger.info("Знайдено {} столиків", tables.size());
        return tables;
    }

    @Cacheable(value = "tableCache", unless = "#result == null")
    public TableDto getTableById(long id) {
        logger.info("Запит на отримання столика з ID: {}", id);
        TableDto tableDto = tableRepository.findById(id)
                .map(tableMapper::toDto)
                .orElseThrow(() -> {
                    logger.error("Стіл з ID {} не знайдено", id);
                    return new TableNotFoundException("Стіл з ID " + id + " не знайдено");
                });
        logger.info("Стіл з ID {} успішно знайдено", id);
        return tableDto;
    }

    public TableDto createTable(TableCreateDto tableCreateDto) {
        logger.info("Створення нового столика з номером {}", tableCreateDto.tableNumber());
        if (tableCreateDto.seats() > MAX_SEATS) {
            logger.warn("Кількість місць {} перевищує максимальне значення {}", tableCreateDto.seats(), MAX_SEATS);
            throw new IllegalArgumentException("Кількість місць за одним столом не може перевищувати " + MAX_SEATS);
        }

        if (tableRepository.existsByTableNumber(tableCreateDto.tableNumber())) {
            logger.warn("Стіл з номером {} вже існує", tableCreateDto.tableNumber());
            throw new IllegalArgumentException("Стіл з таким номером вже існує");
        }

        Tables table = tableMapper.toEntity(tableCreateDto);
        Tables savedTable = tableRepository.save(table);
        logger.info("Стіл з номером {} успішно створено", tableCreateDto.tableNumber());
        return tableMapper.toDto(savedTable);
    }

    public TableDto updateTable(long id, TableDto tableDto) {
        logger.info("Оновлення столика з ID: {}", id);
        if (tableDto.seats() <= 0) {
            logger.warn("Недопустима кількість місць: {}", tableDto.seats());
            throw new IllegalArgumentException("Кількість місць повинна бути більше нуля");
        }

        if (tableDto.seats() > MAX_SEATS) {
            logger.warn("Кількість місць {} перевищує максимальне значення {}", tableDto.seats(), MAX_SEATS);
            throw new IllegalArgumentException("Кількість місць за одним столом не може перевищувати " + MAX_SEATS);
        }

        if (tableRepository.existsByTableNumberAndIdNot(tableDto.tableNumber(), id)) {
            logger.warn("Стіл з номером {} вже існує", tableDto.tableNumber());
            throw new IllegalArgumentException("Стіл з таким номером вже існує");
        }

        Tables table = tableRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Стіл з ID {} не знайдено", id);
                    return new TableNotFoundException("Стіл з ID " + id + " не знайдено");
                });

        table.setTableNumber(tableDto.tableNumber());
        table.setSeats(tableDto.seats());
        table.setReserved(tableDto.isReserved());

        if (tableDto.isReserved()) {
            logger.info("Резервування столика з ID: {} користувачем ID: {}", id, tableDto.reservedByUserId());
            User user = userRepository.findById(tableDto.reservedByUserId())
                    .orElseThrow(() -> {
                        logger.error("Користувач з ID {} не знайдений", tableDto.reservedByUserId());
                        return new UserNotFoundException("Користувач з ID " + tableDto.reservedByUserId() + " не знайдений");
                    });

            table.setReservedByUser(user);
            table.setReservedAt(LocalDateTime.now());
        } else {
            logger.info("Скасування резервування столика з ID: {}", id);
            table.setReservedByUser(null);
            table.setReservedAt(null);
        }

        Tables updatedTable = tableRepository.save(table);
        logger.info("Стіл з ID {} успішно оновлено", id);
        return tableMapper.toDto(updatedTable);
    }

    public void deleteTable(long id) {
        logger.info("Видалення столика з ID: {}", id);
        if (!tableRepository.existsById(id)) {
            logger.error("Стіл з ID {} не знайдено для видалення", id);
            throw new TableNotFoundException("Стіл з ID " + id + " не знайдено для видалення");
        }
        tableRepository.deleteById(id);
        logger.info("Стіл з ID {} успішно видалено", id);
    }

    public TableDto reserveTable(long id) {
        logger.info("Резервування столика з ID: {}", id);
        Tables table = tableRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Стіл з ID {} не знайдено", id);
                    return new TableNotFoundException("Стіл з ID " + id + " не знайдено");
                });

        if (table.isReserved()) {
            logger.warn("Стіл з ID {} вже зарезервований", id);
            throw new RuntimeException("Стіл вже зарезервований");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    logger.error("Користувач з емейлом {} не знайдений", username);
                    return new UserNotFoundException("Користувач з емейлом " + username + " не знайдений");
                });

        table.setReserved(true);
        table.setReservedByUser(user);
        table.setReservedAt(LocalDateTime.now());

        Tables reservedTable = tableRepository.save(table);
        logger.info("Стіл з ID {} успішно зарезервовано користувачем {}", id, username);
        return tableMapper.toDto(reservedTable);
    }

    public TableDto cancelReservation(long id) {
        logger.info("Скасування резервування столика з ID: {}", id);
        Tables table = tableRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Стіл з ID {} не знайдено", id);
                    return new TableNotFoundException("Стіл з ID " + id + " не знайдено");
                });

        if (!table.isReserved()) {
            logger.warn("Стіл з ID {} не зарезервований", id);
            throw new RuntimeException("Стіл не зарезервований");
        }

        table.setReserved(false);
        table.setReservedByUser(null);
        table.setReservedAt(null);

        Tables updatedTable = tableRepository.save(table);
        logger.info("Резервування столика з ID {} успішно скасовано", id);
        return tableMapper.toDto(updatedTable);
    }

    public List<TableDto> getAvailableTables() {
        logger.info("Отримання списку вільних столиків...");
        List<TableDto> availableTables = tableRepository.findByIsReservedFalse().stream()
                .map(tableMapper::toDto)
                .toList();
        logger.info("Знайдено {} вільних столиків", availableTables.size());
        return availableTables;
    }
}
