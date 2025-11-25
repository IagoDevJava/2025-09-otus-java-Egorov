package ru.otus.service.impl;

import java.util.EnumMap;
import java.util.List;
import lombok.AllArgsConstructor;
import ru.otus.dto.BalanceDto;
import ru.otus.dto.DeliveryDto;
import ru.otus.dto.DeliveryDto.NoteDispensed;
import ru.otus.model.BanknoteValue;
import ru.otus.model.UserInsertRequest;
import ru.otus.model.UserIssueRequest;
import ru.otus.service.CalculateService;

@AllArgsConstructor
public class CalculateServiceImpl implements CalculateService {

    CashVault cashVault;

    @Override
    public BalanceDto insertMoney(UserInsertRequest insertRequest) {

        cashVault.deposit(insertRequest.getUserId(), insertRequest.getDenomination(), insertRequest.getCount());

        return BalanceDto.builder()
                .userId(insertRequest.getUserId())
                .balance(cashVault.getBalance(insertRequest.getUserId()))
                .build();
    }

    @Override
    public DeliveryDto issueMoney(UserIssueRequest issueRequest) {
        EnumMap<BanknoteValue, Integer> withdrawn =
                cashVault.withdraw(issueRequest.getUserId(), issueRequest.getAmount());

        List<NoteDispensed> notes = withdrawn.entrySet().stream()
                .map(entry -> new DeliveryDto.NoteDispensed(entry.getKey().getAmount(), entry.getValue()))
                .toList();

        return DeliveryDto.builder()
                .userId(issueRequest.getUserId())
                .notes(notes)
                .build();
    }
}
