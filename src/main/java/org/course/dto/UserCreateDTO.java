package org.course.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO для створення нового користувача
 */
public record UserCreateDTO(
        @NotNull @NotEmpty @NotBlank String name,
        @NotNull @NotEmpty @NotBlank @Email String email,
        @NotNull @NotEmpty @NotBlank String password,
        String role
) implements Serializable {

    public UserCreateDTO {
        if (role == null || role.isEmpty()) {
            role = "USER";
        }
    }
}
