package org.course.mapper;

import org.course.entity.Order;
import org.course.dto.OrderDto;
import org.course.dto.OrderCreateDTO;
import org.mapstruct.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    Order toEntity(OrderDto orderDto);

    OrderDto toDto(Order order);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Order partialUpdate(OrderDto orderDto, @MappingTarget Order order);

    Order toEntity(OrderCreateDTO orderCreateDTO);

    // Метод для конвертації списку у рядок
    default String mapDishIdsListToString(List<Long> dishIds) {
        if (dishIds == null || dishIds.isEmpty()) {
            return "";
        }
        return dishIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    // Метод для конвертації рядка у список
    default List<Long> mapDishIdsStringToList(String dishIdsString) {
        if (dishIdsString == null || dishIdsString.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(dishIdsString.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}
