package org.course.mapper;

import org.course.entity.Dishes;
import org.course.dto.DishesDto;
import org.course.dto.DishesCreateDTO;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DishesMapper {

    Dishes toEntity(DishesDto dishesDto);

    DishesDto toDto(Dishes dishes);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    Dishes partialUpdate(DishesDto dishesDto, @MappingTarget Dishes dishes);

    Dishes toEntity(DishesCreateDTO dishesCreateDTO);
}

