package org.course.mapper;

import org.course.entity.Dishes;
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

    @Mapping(target = "dishIds", source = "dishes", qualifiedByName = "mapDishesToDishIds")
    @Mapping(target = "userId", source = "user.id")
    OrderDto toDto(Order order);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Order partialUpdate(OrderDto orderDto, @MappingTarget Order order);

    Order toEntity(OrderCreateDTO orderCreateDTO);

    @Named("mapDishesToDishIds")
    static List<Long> mapDishesToDishIds(List<Dishes> dishes) {
        return dishes == null ? List.of() : dishes.stream().map(Dishes::getId).collect(Collectors.toList());
    }

    // Конвертація списку у рядок
    default String mapDishIdsListToString(List<Long> dishIds) {
        if (dishIds == null || dishIds.isEmpty()) {
            return "";
        }
        return dishIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    // Конвертація рядка у список
    default List<Long> mapDishIdsStringToList(String dishIdsString) {
        if (dishIdsString == null || dishIdsString.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(dishIdsString.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}
