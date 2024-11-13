package org.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link org.course.entity.Order}
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link org.course.entity.Order}
 */
public record OrderCreateDTO(
        @NotNull @NotEmpty long userId,
        @NotEmpty List<Long> dishIds,
        double fullprice,
        @NotNull @NotEmpty @NotBlank String addition) implements Serializable {
}
