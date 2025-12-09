package ru.otus.processor.homework;

import java.time.LocalTime;
import ru.otus.model.Message;
import ru.otus.processor.Processor;

public class ProcessorThrowOnEvenSecond implements Processor {

    private final TimeProvider timeProvider;

    public ProcessorThrowOnEvenSecond(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public ProcessorThrowOnEvenSecond() {
        this(LocalTime::now);
    }

    @Override
    public Message process(Message message) {
        int second = timeProvider.currentTime().getSecond();
        if (second % 2 == 0) {
            throw new IllegalStateException("Current second is even: " + second);
        }
        return message;
    }
}
