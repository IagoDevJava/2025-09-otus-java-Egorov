package ru.otus.exception;

public class WithdrawalNotPossibleException extends RuntimeException {

    public WithdrawalNotPossibleException(String message) {
        super(message);
    }
}
