package ru.otus.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.exception.WithdrawalNotPossibleException;
import ru.otus.model.BanknoteValue;

class CashVaultTest {

    private CashVault vault;

    @BeforeEach
    void setUp() {
        vault = new CashVault();
    }

    @Test
    void shouldThrowOnNullUserIdOnDeposit() {
        assertThatThrownBy(() -> vault.deposit(null, BanknoteValue.VALUE_100, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID must not be null");
    }

    @Test
    void shouldThrowOnNullDenominationOnDeposit() {
        assertThatThrownBy(() -> vault.deposit(1L, null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Denomination must not be null");
    }

    @Test
    void shouldThrowOnZeroCountOnDeposit() {
        assertThatThrownBy(() -> vault.deposit(1L, BanknoteValue.VALUE_100, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Count must be positive");
    }

    @Test
    void shouldThrowOnNegativeCountOnDeposit() {
        assertThatThrownBy(() -> vault.deposit(1L, BanknoteValue.VALUE_100, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Count must be positive");
    }

    @Test
    void shouldDepositAndIncreaseBalance() {
        vault.deposit(1L, BanknoteValue.VALUE_100, 2);
        assertThat(vault.getBalance(1L)).isEqualTo(200);
    }

    @Test
    void shouldWithdrawExactAmountWithAvailableNotes() {
        vault.deposit(1L, BanknoteValue.VALUE_100, 3); // 300
        EnumMap<BanknoteValue, Integer> result = vault.withdraw(1L, 200);
        assertThat(result).containsEntry(BanknoteValue.VALUE_100, 2);
        assertThat(vault.getBalance(1L)).isEqualTo(100);
    }

    @Test
    void shouldWithdrawUsingMultipleDenominations() {
        vault.deposit(1L, BanknoteValue.VALUE_100, 1);
        vault.deposit(1L, BanknoteValue.VALUE_50, 2); // total = 200
        EnumMap<BanknoteValue, Integer> result = vault.withdraw(1L, 150);

        // После сортировки: сначала 100, потом 50 → должен взять 1×100 + 1×50
        assertThat(result)
                .containsEntry(BanknoteValue.VALUE_100, 1)
                .containsEntry(BanknoteValue.VALUE_50, 1)
                .hasSize(2);

        assertThat(vault.getBalance(1L)).isEqualTo(50);
    }

    @Test
    void shouldPreferLargerDenominations() {
        // Есть: 1×500, 1×100 → просим 600 → должно выдать 500+100
        vault.deposit(1L, BanknoteValue.VALUE_500, 1);
        vault.deposit(1L, BanknoteValue.VALUE_100, 1);
        EnumMap<BanknoteValue, Integer> result = vault.withdraw(1L, 600);

        assertThat(result)
                .containsEntry(BanknoteValue.VALUE_500, 1)
                .containsEntry(BanknoteValue.VALUE_100, 1)
                .hasSize(2);

        assertThat(vault.getBalance(1L)).isZero();
    }

    @Test
    void shouldFailToWithdrawWhenInsufficientTotalFunds() {
        vault.deposit(1L, BanknoteValue.VALUE_100, 2); // 200
        assertThatThrownBy(() -> vault.withdraw(1L, 300))
                .isInstanceOf(WithdrawalNotPossibleException.class)
                .hasMessage("Cannot withdraw amount: 300 for user 1");
    }

    @Test
    void shouldFailToWithdrawWhenAmountNotFormateDespiteSufficientTotal() {
        // Есть: 2 шт × 500 = 1000, но нужно 300 → нет мелочи!
        vault.deposit(1L, BanknoteValue.VALUE_500, 2);
        assertThatThrownBy(() -> vault.withdraw(1L, 300))
                .isInstanceOf(WithdrawalNotPossibleException.class)
                .hasMessageContaining("Cannot withdraw amount: 300");
    }

    @Test
    void shouldThrowOnZeroOrNegativeWithdrawAmount() {
        assertThatThrownBy(() -> vault.withdraw(1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be positive");

        assertThatThrownBy(() -> vault.withdraw(1L, -50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be positive");
    }

    @Test
    void shouldRemoveUserWhenVaultBecomesEmpty() {
        vault.deposit(1L, BanknoteValue.VALUE_100, 1);
        vault.withdraw(1L, 100);
        assertThat(vault.getBalance(1L)).isZero();
    }
}
