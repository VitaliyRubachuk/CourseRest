package org.course.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.course.entity.OrderStatus;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link org.course.entity.Order}
 */
public record OrderDto(
        long id,

        @NotNull(message = "User ID не може бути порожнім")
        long userId,

        @NotEmpty(message = "Список страв не може бути порожнім")
        List<Long> dishIds,

        @NotNull(message = "Ціна не може бути порожньою")
        double fullPrice,

        String addition,

        OrderStatus status) implements Serializable {
}
