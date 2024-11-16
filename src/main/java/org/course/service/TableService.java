package org.course.service;

import org.course.dto.TableCreateDto;
import org.course.dto.TableDto;
import org.course.entity.Tables;
import org.course.mapper.TableMapper;
import org.course.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.course.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TableService {

    private final TableRepository tableRepository;
    private final TableMapper tableMapper;

    @Autowired
    public TableService(TableRepository tableRepository, TableMapper tableMapper) {
        this.tableRepository = tableRepository;
        this.tableMapper = tableMapper;
    }

    public List<TableDto> getAllTables() {
        return tableRepository.findAll().stream()
                .map(tableMapper::toDto)
                .toList();
    }

    public TableDto getTableById(long id) {
        return tableRepository.findById(id)
                .map(tableMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Table not found"));
    }

    public TableDto createTable(TableCreateDto tableCreateDto) {
        if (tableRepository.existsByTableNumber(tableCreateDto.tableNumber())) {
            throw new RuntimeException("Table with this number already exists");
        }
        Tables table = tableMapper.toEntity(tableCreateDto);
        Tables savedTable = tableRepository.save(table);
        return tableMapper.toDto(savedTable);
    }

    public TableDto updateTable(long id, TableDto tableDto) {
        Tables existingTable = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        tableMapper.partialUpdate(tableDto, existingTable);
        Tables updatedTable = tableRepository.save(existingTable);
        return tableMapper.toDto(updatedTable);
    }

    public void deleteTable(long id) {
        if (!tableRepository.existsById(id)) {
            throw new RuntimeException("Table not found");
        }
        tableRepository.deleteById(id);
    }

    public TableDto reserveTable(long id, long userId) {
        Tables table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        if (table.isReserved()) {
            throw new RuntimeException("Table is already reserved");
        }

        User user = new User(userId);
        table.setReserved(true);
        table.setReservedByUser(user);
        table.setReservedAt(LocalDateTime.now());

        Tables reservedTable = tableRepository.save(table);

        return tableMapper.toDto(reservedTable);
    }

    public TableDto cancelReservation(long id) {
        Tables table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        if (!table.isReserved()) {
            throw new RuntimeException("Table is not reserved");
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
