package ru.otus.service;

import ru.otus.dto.BalanceDto;
import ru.otus.dto.DeliveryDto;
import ru.otus.model.UserInsertRequest;
import ru.otus.model.UserIssueRequest;

public interface CalculateService {

    BalanceDto insertMoney(UserInsertRequest request);

    DeliveryDto issueMoney(UserIssueRequest issueRequest);
}
