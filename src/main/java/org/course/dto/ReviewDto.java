package org.course.dto;

import java.time.LocalDateTime;

public record ReviewDto(
        Long id,
        long userId,
        Long dishId,
        String comment,
        int rating,
        LocalDateTime createdAt
) {
}
