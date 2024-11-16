package org.course.controller;

import org.course.dto.TableCreateDto;
import org.course.dto.TableDto;
import org.course.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public TableDto createTable(@RequestBody TableCreateDto tableCreateDto) {
        return tableService.createTable(tableCreateDto);
    }

    @PutMapping("/{id}")
    public TableDto updateTable(@PathVariable long id, @RequestBody TableDto tableDto) {
        return tableService.updateTable(id, tableDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable long id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reserve")
    public TableDto reserveTable(@PathVariable long id, @RequestParam long userId) {
        return tableService.reserveTable(id, userId);
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
