package org.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for {@link org.course.entity.User}
 */
public record UserCreateDTO(@NotNull @NotEmpty @NotBlank String name) implements Serializable { }
