package org.course.dto;

public record ReviewCreateDTO(
        Long userId,
        Long dishId,
        String comment,
        int rating
) {
}
