package com.erbol.ems.event;

import com.erbol.ems.event.dto.EventCardDto;
import com.erbol.ems.event.dto.EventDetailDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * Maps Event entities to DTOs.
 *
 * <p>Implemented automatically by MapStruct at compile time. The
 * generated class is named EventMapperImpl and registered as a Spring bean
 * via componentModel = "spring".
 */
@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "organizerName", source = "organizer.fullName")
    @Mapping(target = "shortDescription", source = "description", qualifiedByName = "shorten")
    @Mapping(target = "free", expression = "java(event.isFree())")
    EventCardDto toCardDto(Event event);

    List<EventCardDto> toCardDtoList(List<Event> events);

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "organizerName", source = "organizer.fullName")
    @Mapping(target = "free", expression = "java(event.isFree())")
    @Mapping(target = "status", source = "status")
    EventDetailDto toDetailDto(Event event);

    @Named("shorten")
    default String shorten(String text) {
        if (text == null) {
            return null;
        }
        return text.length() <= 140 ? text : text.substring(0, 137) + "...";
    }
}
