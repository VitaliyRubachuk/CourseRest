package org.course.dto;

import java.io.Serializable;

/**
 * DTO for {@link org.course.entity.Dishes}
 */
public record DishesDto(long id, String name, String price, String description) implements Serializable {
}