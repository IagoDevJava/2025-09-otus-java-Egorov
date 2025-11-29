package ru.otus.service.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ru.otus.exception.WithdrawalNotPossibleException;
import ru.otus.model.BanknoteValue;

public class CashVault implements Vault {

    private final Map<Long, EnumMap<BanknoteValue, Integer>> cells = new ConcurrentHashMap<>();
    private static final BanknoteValue[] DESCENDING_NOMINALS = Arrays.stream(BanknoteValue.values())
            .sorted(Comparator.comparingInt(BanknoteValue::getAmount).reversed())
            .toArray(BanknoteValue[]::new);

    @Override
    public void deposit(Long userId, BanknoteValue denomination, int count) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        if (denomination == null) {
            throw new IllegalArgumentException("Denomination must not be null");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }

        cells.computeIfAbsent(userId, k -> new EnumMap<>(BanknoteValue.class)).merge(denomination, count, Integer::sum);
    }

    @Override
    public EnumMap<BanknoteValue, Integer> withdraw(Long userId, int amount) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        EnumMap<BanknoteValue, Integer> userCells = cells.getOrDefault(userId, new EnumMap<>(BanknoteValue.class));
        EnumMap<BanknoteValue, Integer> withdrawal = new EnumMap<>(BanknoteValue.class);
        int remaining = amount;

        for (BanknoteValue nominal : DESCENDING_NOMINALS) {
            int nominalValue = nominal.getAmount();
            int available = userCells.getOrDefault(nominal, 0);

            if (available > 0 && nominalValue <= remaining) {
                int needed = remaining / nominalValue;
                int toWithdraw = Math.min(needed, available);
                if (toWithdraw > 0) {
                    withdrawal.put(nominal, toWithdraw);
                    remaining -= toWithdraw * nominalValue;
                }
            }
        }

        if (remaining != 0) {
            throw new WithdrawalNotPossibleException("Cannot withdraw amount: " + amount + " for user " + userId);
        }

        // уменьшаем баланс
        EnumMap<BanknoteValue, Integer> current = cells.get(userId);
        if (current == null) {
            throw new IllegalStateException("User " + userId + " has no vault after successful withdrawal check");
        }

        for (Map.Entry<BanknoteValue, Integer> entry : withdrawal.entrySet()) {
            BanknoteValue nominal = entry.getKey();
            int withdrawnCount = entry.getValue();
            int newCount = current.get(nominal) - withdrawnCount;
            if (newCount == 0) {
                current.remove(nominal);
            } else {
                current.put(nominal, newCount);
            }
        }

        // Удаляем пустой vault, если все ячейки пусты (опционально)
        if (current.isEmpty()) {
            cells.remove(userId);
        }

        return withdrawal;
    }

    @Override
    public int getBalance(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        return cells.getOrDefault(userId, new EnumMap<>(BanknoteValue.class)).entrySet().stream()
                .mapToInt(e -> e.getKey().getAmount() * e.getValue())
                .sum();
    }
}
