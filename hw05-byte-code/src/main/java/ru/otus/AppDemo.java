package ru.otus;

import ru.otus.logging.LoggingProxy;
import ru.otus.logging.TestLogging;
import ru.otus.logging.TestLoggingImpl;

public class AppDemo {

    public static void main(String[] args) {
        TestLogging logging = LoggingProxy.createProxy(new TestLoggingImpl());
        logging.calculation(6);
        logging.calculation(6, 7);
        logging.calculation(6, 7, "test");
    }
}
