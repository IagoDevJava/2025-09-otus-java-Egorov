package ru.otus.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.otus.dto.BalanceDto;
import ru.otus.dto.DeliveryDto;
import ru.otus.model.UserInsertRequest;
import ru.otus.model.UserIssueRequest;
import ru.otus.service.CalculateService;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ATMController {

    CalculateService calculateService;

    public BalanceDto insertMoney(UserInsertRequest request) {

        return calculateService.insertMoney(request);
    }

    public DeliveryDto issueMoney(UserIssueRequest request) {

        return calculateService.issueMoney(request);
    }
}
