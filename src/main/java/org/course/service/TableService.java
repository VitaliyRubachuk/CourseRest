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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TableService {

    private final UserRepository userRepository;

    @Autowired
    public TableService(TableRepository tableRepository, TableMapper tableMapper, UserRepository userRepository) {
        this.tableRepository = tableRepository;
        this.tableMapper = tableMapper;
        this.userRepository = userRepository;
    }


    private static final int MAX_SEATS = 30;
    private static final Logger logger = LoggerFactory.getLogger(DishesService.class);
    private final TableRepository tableRepository;
    private final TableMapper tableMapper;


    public List<TableDto> getAllTables() {
        return tableRepository.findAll().stream()
                .map(tableMapper::toDto)
                .toList();
    }


    @Cacheable(value = "tableCache", unless = "#result == null")
    public TableDto getTableById(long id) {
        logger.info("Запит на отримання столика..");
        return tableRepository.findById(id)
                .map(tableMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Стіл не знайдено"));
    }

    public TableDto createTable(TableCreateDto tableCreateDto) {
        if (tableCreateDto.seats() > MAX_SEATS) {
            throw new IllegalArgumentException("Кількість місць за одним столом не може перевищувати " + MAX_SEATS);
        }

        if (tableRepository.existsByTableNumber(tableCreateDto.tableNumber())) {
            throw new IllegalArgumentException("Стіл з таким номером вже існує");
        }

        Tables table = tableMapper.toEntity(tableCreateDto);
        Tables savedTable = tableRepository.save(table);
        return tableMapper.toDto(savedTable);
    }

    public TableDto updateTable(long id, TableDto tableDto) {
        if (tableDto.seats() <= 0) {
            throw new IllegalArgumentException("Кількість місць повинна бути більше нуля");
        }

        if (tableDto.seats() > MAX_SEATS) {
            throw new IllegalArgumentException("Кількість місць за одним столом не може перевищувати " + MAX_SEATS);
        }

        if (tableRepository.existsByTableNumberAndIdNot(tableDto.tableNumber(), id)) {
            throw new IllegalArgumentException("Стіл з таким номером вже існує");
        }

        Tables table = tableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Стіл не знайдено"));

        table.setTableNumber(tableDto.tableNumber());
        table.setSeats(tableDto.seats());
        table.setReserved(tableDto.isReserved());

        if (tableDto.isReserved()) {
            User user = userRepository.findById(tableDto.reservedByUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Користувач не знайдений"));

            table.setReservedByUser(user);
            table.setReservedAt(LocalDateTime.now());
        } else {
            table.setReservedByUser(null);
            table.setReservedAt(null);
        }

        Tables updatedTable = tableRepository.save(table);
        return tableMapper.toDto(updatedTable);
    }



    public void deleteTable(long id) {
        if (!tableRepository.existsById(id)) {
            throw new RuntimeException("Стіл не знайдено");
        }
        tableRepository.deleteById(id);
    }

    public TableDto reserveTable(long id) {
        Tables table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Стіл не знайдено"));

        if (table.isReserved()) {
            throw new RuntimeException("Стіл вже зарезервований");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Користувач не знайдений"));

        table.setReserved(true);
        table.setReservedByUser(user);
        table.setReservedAt(LocalDateTime.now());

        Tables reservedTable = tableRepository.save(table);
        return tableMapper.toDto(reservedTable);
    }


    public TableDto cancelReservation(long id) {
        Tables table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Стіл не знайдено"));

        if (!table.isReserved()) {
            throw new RuntimeException("Стіл не зарезервований");
        }

        table.setReserved(false);
        table.setReservedByUser(null);
        table.setReservedAt(null);

        Tables updatedTable = tableRepository.save(table);

        return tableMapper.toDto(updatedTable);
    }

    public List<TableDto> getAvailableTables() {
        return tableRepository.findByIsReservedFalse().stream()
                .map(tableMapper::toDto)
                .toList();
    }
}
