package ru.otus.model;

import lombok.Getter;

@Getter
public enum BanknoteValue {
    VALUE_10(10),
    VALUE_50(50),
    VALUE_100(100),
    VALUE_500(500),
    VALUE_1000(1000);

    private final int amount;

    BanknoteValue(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Banknote amount must be positive");
        }
        this.amount = amount;
    }
}
