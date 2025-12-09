package ru.otus.processor.homework;

import java.time.LocalTime;

@FunctionalInterface
public interface TimeProvider {

    LocalTime currentTime();
}
