package org.course.controller;

import jakarta.validation.Valid;
import org.course.dto.UserCreateDTO;
import org.course.dto.UserDto;
import org.course.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable long id) {
        Optional<UserDto> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO, BindingResult result) {
        if (result.hasErrors()) {
            // Якщо є помилки валідації, формуємо список повідомлень
            StringBuilder errors = new StringBuilder();
            result.getAllErrors().forEach(error -> errors.append(error.getDefaultMessage()).append("\n"));

            // Повертаємо статус BAD_REQUEST та повідомлення про помилки
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.toString());
        }

        try {
            UserDto createdUser = userService.createUser(userCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            // Якщо виникає помилка (наприклад, email вже існує), повертаємо 400 зі специфічною помилкою
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }



    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable long id, @Valid @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable long id) {
        try {
            userService.deleteUser(id);
            // Повертаємо повідомлення про успішне видалення
            return ResponseEntity.ok("Користувача успішно видалено.");
        } catch (RuntimeException e) {
            // Якщо виникає помилка (наприклад, користувача не знайдено), повертаємо 404
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Користувача не знайдено.");
        }
    }

}
