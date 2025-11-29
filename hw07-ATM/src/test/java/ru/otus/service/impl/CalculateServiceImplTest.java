package ru.otus.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.dto.BalanceDto;
import ru.otus.dto.DeliveryDto;
import ru.otus.exception.WithdrawalNotPossibleException;
import ru.otus.model.BanknoteValue;
import ru.otus.model.UserInsertRequest;
import ru.otus.model.UserIssueRequest;

class CalculateServiceImplTest {

    private CashVault vault;
    private CalculateServiceImpl service;

    @BeforeEach
    void setUp() {
        vault = new CashVault();
        service = new CalculateServiceImpl(vault);
    }

    @Test
    void shouldInsertMoneyAndReturnBalanceDto() {
        UserInsertRequest request = new UserInsertRequest(1L, BanknoteValue.VALUE_500, 2);
        BalanceDto result = service.insertMoney(request);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getBalance()).isEqualTo(1000);
    }

    @Test
    void shouldIssueMoneyAndReturnDeliveryDto() {
        vault.deposit(1L, BanknoteValue.VALUE_1000, 1);
        vault.deposit(1L, BanknoteValue.VALUE_500, 2);

        UserIssueRequest request = new UserIssueRequest(1L, 1500);
        DeliveryDto result = service.issueMoney(request);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getNotes()).hasSize(2);
        assertThat(result.getNotes())
                .anySatisfy(note -> assertThat(note).isEqualTo(new DeliveryDto.NoteDispensed(1000, 1)));
        assertThat(result.getNotes())
                .anySatisfy(note -> assertThat(note).isEqualTo(new DeliveryDto.NoteDispensed(500, 1)));
    }

    @Test
    void shouldThrowOnImpossibleWithdrawalInIssueMoney() {
        vault.deposit(1L, BanknoteValue.VALUE_500, 2);
        vault.deposit(1L, BanknoteValue.VALUE_1000, 1); // итого: 500, 500, 1000
        UserIssueRequest request = new UserIssueRequest(1L, 600);

        assertThatThrownBy(() -> service.issueMoney(request))
                .isExactlyInstanceOf(WithdrawalNotPossibleException.class)
                .hasMessageContaining("Cannot withdraw amount: 600");
    }
}
