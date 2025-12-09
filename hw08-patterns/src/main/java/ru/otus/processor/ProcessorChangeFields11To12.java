package ru.otus.processor;

import ru.otus.model.Message;

public class ProcessorChangeFields11To12 implements Processor {

    @Override
    public Message process(Message message) {
        if (message == null) {
            return null;
        }

        String tempField11 = message.getField11();
        String tempField12 = message.getField12();

        return message.toBuilder().field11(tempField12).field12(tempField11).build();
    }
}
