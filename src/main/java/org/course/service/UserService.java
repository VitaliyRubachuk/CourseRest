package org.course.service;

import org.course.entity.Tables;
import jakarta.annotation.PostConstruct;
import org.course.entity.Role;
import org.course.entity.User;
import org.course.repository.UserRepository;
import org.course.repository.TableRepository;
import org.course.mapper.UserMapper;
import org.course.dto.UserDto;
import org.course.dto.UserCreateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TableRepository tableRepository;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper, TableRepository tableRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.tableRepository = tableRepository;  // Ініціалізація TableRepository
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    public Optional<UserDto> getUserById(long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }

    public UserDto createUser(UserCreateDTO userCreateDTO) {
        System.out.println("Creating user with name: " + userCreateDTO.name());

        // Перевіряємо, чи існує вже користувач з таким email
        if (userRepository.findByEmail(userCreateDTO.email()).isPresent()) {
            throw new RuntimeException("Email вже використовується");
        }

        // Якщо роль не вказана, присвоюємо "USER"
        if (userCreateDTO.role() == null || userCreateDTO.role().isEmpty()) {
            userCreateDTO = new UserCreateDTO(userCreateDTO.name(), userCreateDTO.email(), userCreateDTO.password(), "USER");
        }

        User user = userMapper.toEntity(userCreateDTO);

        // Хешуємо пароль перед збереженням
        user.setPassword(userCreateDTO.password());

        // Зберігаємо нового користувача
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }


    @PostConstruct
    public void createDefaultAdmin() {
        if (userRepository.findByEmail("admin@a").isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@a");
            admin.setPassword("admin");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }
    }

    public UserDto updateUser(long id, UserDto userDto) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        userMapper.partialUpdate(userDto, existingUser);
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Користувач не знайдений"));

        // Очистити поле user у всіх коментарях
        user.getReviews().forEach(review -> review.setUser(null));

        // Очистити всі резервування користувача на столах
        user.getReservedTables().forEach(table -> {
            table.setReserved(false);
            table.setReservedByUser(null);
            table.setReservedAt(null);
        });

        // Видалити користувача
        userRepository.delete(user);
    }


}
