package org.course.dto;

import java.io.Serializable;

/**
 * DTO for {@link org.course.entity.User}
 */
public record UserDto(long id, String name) implements Serializable
{ }