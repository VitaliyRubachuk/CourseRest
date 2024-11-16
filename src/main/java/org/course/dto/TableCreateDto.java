package org.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record TableCreateDto(
        @NotNull @Min(1) int tableNumber,
        @NotNull @Min(1) int seats
) implements Serializable { }
