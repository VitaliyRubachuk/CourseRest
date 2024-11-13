package org.course.mapper;

import org.course.entity.Dishes;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapperHelper
{

    // конвертація `List<Dishes>` у `List<Long>`
    @Named("mapDishesToDishIds")
    public List<Long> mapDishesToDishIds(List<Dishes> dishes) {
        if (dishes == null || dishes.isEmpty()) {
            return List.of();
        }
        return dishes.stream()
                .map(Dishes::getId)
                .collect(Collectors.toList());
    }
}
