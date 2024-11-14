    package org.course.dto;
    import org.course.entity.OrderStatus;

    import java.io.Serializable;
    import java.util.List;

    /**
     * DTO for {@link org.course.entity.Order}
     */
    public record OrderDto(long id, long userId, List<Long> dishIds, double fullPrice, String addition, OrderStatus status) implements Serializable {}
