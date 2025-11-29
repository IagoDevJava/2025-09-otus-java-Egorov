package ru.otus.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.dto.BalanceDto;
import ru.otus.dto.DeliveryDto;
import ru.otus.model.BanknoteValue;
import ru.otus.model.UserInsertRequest;
import ru.otus.model.UserIssueRequest;
import ru.otus.service.impl.CalculateServiceImpl;
import ru.otus.service.impl.CashVault;

class ATMControllerTest {

    private ATMController controller;

    @BeforeEach
    void setUp() {
        CashVault vault = new CashVault();
        CalculateServiceImpl service = new CalculateServiceImpl(vault);
        controller = new ATMController(service);
    }

    @Test
    void shouldInsertMoneyViaController() {
        UserInsertRequest request = new UserInsertRequest(1L, BanknoteValue.VALUE_10, 5);
        BalanceDto result = controller.insertMoney(request);
        assertThat(result.getBalance()).isEqualTo(50);
    }

    @Test
    void shouldIssueMoneyViaController() {
        // Добавим сначала деньги через контроллер
        controller.insertMoney(new UserInsertRequest(1L, BanknoteValue.VALUE_500, 3));

        UserIssueRequest request = new UserIssueRequest(1L, 1000);
        DeliveryDto result = controller.issueMoney(request);

        assertThat(result.getNotes()).hasSize(1);
        assertThat(result.getNotes().getFirst()).isEqualTo(new DeliveryDto.NoteDispensed(500, 2));
    }
}
