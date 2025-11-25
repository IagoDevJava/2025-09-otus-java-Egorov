package ru.otus.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserIssueRequest {

    @NonNull
    Long userId;

    @NonNull
    Integer amount;
}
