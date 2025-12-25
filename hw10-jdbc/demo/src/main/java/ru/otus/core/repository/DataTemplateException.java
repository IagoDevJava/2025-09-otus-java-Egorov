package ru.otus.core.repository;

public class DataTemplateException extends RuntimeException {

    public DataTemplateException(String message) {
        super(message);
    }

    public DataTemplateException(Exception ex) {
        super(ex);
    }
}
