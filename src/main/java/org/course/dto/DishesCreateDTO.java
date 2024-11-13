package org.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record DishesCreateDTO(
        @NotNull @NotEmpty @NotBlank String name,
        String price,
        String description) implements Serializable {
}