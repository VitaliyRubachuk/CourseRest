package org.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link org.course.entity.Order}
 */
public record OrderCreateDTO(
        @NotNull(message = "User ID не може бути порожнім")
        long userId,

        @NotEmpty(message = "Список страв не може бути порожнім")
        List<Long> dishIds,

        String addition,

        String status) implements Serializable {
}
