package ru.otus.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryDto {

    private Long userId;
    private List<NoteDispensed> notes;

    public record NoteDispensed(int denomination, int count) {}
}
