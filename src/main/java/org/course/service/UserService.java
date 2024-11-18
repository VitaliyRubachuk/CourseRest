package org.course.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import org.course.entity.Role;
import org.course.entity.User;
import org.course.repository.UserRepository;
import org.course.repository.TableRepository;
import org.course.mapper.UserMapper;
import org.course.dto.UserDto;
import org.course.dto.UserCreateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper, TableRepository tableRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.tableRepository = tableRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        logger.error("Користувач з ім'ям {} не знайдений", username);
                        return new UsernameNotFoundException("User not found with username: " + username);
                    });
            return org.springframework.security.core.userdetails.User
                    .builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
        } catch (Exception e) {
            logger.error("Помилка під час завантаження користувача: {}", e.getMessage());
            throw e;
        }
    }


    public List<UserDto> getAllUsers() {
        logger.info("Запит на отримання всіх користувачів");
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Cacheable(cacheNames = "users")
    public Optional<UserDto> getUserById(long id) {
        logger.info("Запит на отримання користувача з ID: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .or(() -> {
                    logger.warn("Користувача з ID {} не знайдено", id);
                    return Optional.empty();
                });
    }

    public UserDto createUser(UserCreateDTO userCreateDTO) {
        logger.info("Створення користувача з ім'ям: {}", userCreateDTO.name());
        if (userRepository.findByEmail(userCreateDTO.email()).isPresent()) {
            logger.error("Email {} вже використовується", userCreateDTO.email());
            throw new RuntimeException("Email вже використовується");
        }

        User user = userMapper.toEntity(userCreateDTO);
        user.setPassword(userCreateDTO.password());
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);
        logger.info("Користувача створено успішно з ID: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @PostConstruct
    public void createDefaultAdmin() {
        logger.info("Перевірка наявності адміністратора за замовчуванням");
        if (userRepository.findByEmail("admin@a").isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@a");
            admin.setPassword("admin");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            logger.info("Адміністратор за замовчуванням створений");
        } else {
            logger.info("Адміністратор за замовчуванням вже існує");
        }
    }

    public UserDto updateUser(long id, UserDto userDto) {
        logger.info("Оновлення користувача з ID: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Користувача з ID {} не знайдено", id);
                    return new RuntimeException("User not found");
                });

        userMapper.partialUpdate(userDto, existingUser);
        User updatedUser = userRepository.save(existingUser);
        logger.info("Користувач з ID {} оновлений успішно", updatedUser.getId());
        return userMapper.toDto(updatedUser);
    }

    public void deleteUser(Long id) {
        logger.info("Видалення користувача з ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Користувача з ID {} не знайдено", id);
                    return new RuntimeException("Користувач не знайдений");
                });

        user.getReviews().forEach(review -> review.setUser(null));
        user.getReservedTables().forEach(table -> {
            table.setReserved(false);
            table.setReservedByUser(null);
            table.setReservedAt(null);
        });

        userRepository.delete(user);
        logger.info("Користувача з ID {} видалено успішно", id);
    }
}
