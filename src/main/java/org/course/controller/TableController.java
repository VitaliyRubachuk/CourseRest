package org.course.controller;

import jakarta.validation.Valid;
import org.course.dto.TableCreateDto;
import org.course.dto.TableDto;
import org.course.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final TableService tableService;

    @Autowired
    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public List<TableDto> getAllTables() {
        return tableService.getAllTables();
    }

    @GetMapping("/{id}")
    public TableDto getTableById(@PathVariable long id) {
        return tableService.getTableById(id);
    }

    @PostMapping
    public ResponseEntity<Object> createTable(@Valid @RequestBody TableCreateDto tableCreateDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }

        TableDto createdTable = tableService.createTable(tableCreateDto);
        return ResponseEntity.ok(createdTable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateTable(@PathVariable long id, @Valid @RequestBody TableDto tableDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }

        TableDto updatedTable = tableService.updateTable(id, tableDto);
        return ResponseEntity.ok(updatedTable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTable(@PathVariable long id) {
        try {
            tableService.deleteTable(id);
            return ResponseEntity.ok("Стіл успішно видалено.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Стіл не знайдено.");
        }
    }


    @PostMapping("/{id}/reserve")
    public ResponseEntity<TableDto> reserveTable(@PathVariable long id) {
        TableDto reservedTable = tableService.reserveTable(id);
        return ResponseEntity.ok(reservedTable);
    }

    @DeleteMapping("/{id}/reserve")
    public TableDto cancelReservation(@PathVariable long id) {
        return tableService.cancelReservation(id);
    }

    @GetMapping("/available")
    public List<TableDto> getAvailableTables() {
        return tableService.getAvailableTables();
    }
}

