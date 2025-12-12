package ru.otus.processor.homework;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import ru.otus.model.Message;

class ProcessorThrowOnEvenSecondTest {

    @Test
    void shouldThrowExceptionWhenSecondIsEven() {
        // Arrange
        TimeProvider timeProvider = () -> LocalTime.of(10, 5, 42);
        var processor = new ProcessorThrowOnEvenSecond(timeProvider);
        var message = new Message.Builder(1L).build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> processor.process(message));
        assertTrue(exception.getMessage().contains("even"));
    }

    @Test
    void shouldNotThrowExceptionWhenSecondIsOdd() {
        // Arrange
        TimeProvider timeProvider = () -> LocalTime.of(10, 5, 43);
        var processor = new ProcessorThrowOnEvenSecond(timeProvider);
        var message = new Message.Builder(1L).build();

        // Act & Assert
        assertDoesNotThrow(() -> processor.process(message));
    }
}
