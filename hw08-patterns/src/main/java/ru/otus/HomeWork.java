package ru.otus;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.handler.ComplexProcessor;
import ru.otus.listener.ListenerPrinterConsole;
import ru.otus.listener.homework.HistoryListener;
import ru.otus.model.Message;
import ru.otus.model.ObjectForMessage;
import ru.otus.processor.ProcessorChangeFields11To12;
import ru.otus.processor.ProcessorConcatFields;
import ru.otus.processor.ProcessorUpperField10;
import ru.otus.processor.homework.ProcessorThrowOnEvenSecond;

public class HomeWork {

    private static final Logger logger = LoggerFactory.getLogger(HomeWork.class);

    public static void main(String[] args) {
        var complexProcessor = getComplexProcessor();

        // Подписчики
        var consoleListener = new ListenerPrinterConsole();
        var historyListener = new HistoryListener();

        complexProcessor.addListener(consoleListener);
        complexProcessor.addListener(historyListener);

        // Подготавливаем данные для field13
        var field13 = new ObjectForMessage();
        var data = new ArrayList<String>();
        data.add("history-safe-data");
        field13.setData(data);

        // Создаём сообщение с field11, field12, field13
        var message = new Message.Builder(100L)
                .field1("f1")
                .field2("f2")
                .field3("f3")
                .field10("original-field10")
                .field11("value11")
                .field12("value12")
                .field13(field13)
                .build();

        logger.info("Original message: {}", message);

        // Обрабатываем
        var result = complexProcessor.handle(message);

        logger.info("Processed message: {}", result);

        // Проверяем историю
        var fromHistory = historyListener.findMessageById(100L);
        if (fromHistory.isPresent()) {
            logger.info("Message from history: {}", fromHistory.get());
        } else {
            logger.warn("Message not found in history!");
        }

        // Убираем слушателей
        complexProcessor.removeListener(consoleListener);
        complexProcessor.removeListener(historyListener);
    }

    private static ComplexProcessor getComplexProcessor() {
        var swapProcessor = new ProcessorChangeFields11To12();
        var evenSecondProcessor = new ProcessorThrowOnEvenSecond();
        var concatProcessor = new ProcessorConcatFields();
        var upperProcessor = new ProcessorUpperField10();

        var processors = List.of(concatProcessor, upperProcessor, swapProcessor, evenSecondProcessor);

        return new ComplexProcessor(processors, ex -> {
            logger.error("Processor error: ", ex);
        });
    }
}
