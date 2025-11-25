package ru.otus.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInsertRequest {

    @NonNull
    Long userId;

    @NonNull
    BanknoteValue denomination;

    @NonNull
    Integer count;
}
