    package org.course.dto;

    import java.io.Serializable;
    import java.util.List;

    /**
     * DTO for {@link org.course.entity.Order}
     */
    public record OrderDto(long id, long userId, List<Long> dishIds, double fullprice, String addition) implements Serializable {}
