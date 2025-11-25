package ru.otus.service.impl;

import java.util.EnumMap;
import ru.otus.model.BanknoteValue;

public interface Vault {

    void deposit(Long userId, BanknoteValue denomination, int count);

    EnumMap<BanknoteValue, Integer> withdraw(Long userId, int amount);

    int getBalance(Long userId);
}
