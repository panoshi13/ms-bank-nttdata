package com.ntt.data.ms.bank.accounts.mapper;

import com.ntt.data.ms.bank.accounts.entity.BankAccount;
import com.ntt.data.ms.bank.accounts.model.BankAccountResponse;
import com.ntt.data.ms.bank.accounts.model.BankAccountResponseHolders;
import com.ntt.data.ms.bank.accounts.model.BankAccountResponseMovements;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BankAccountMapper {
    public BankAccountResponse mapToBankAccountResponse(BankAccount bankAccount) {
        var bankAccountResponse = new BankAccountResponse();
        bankAccountResponse.id(String.valueOf(bankAccount.getId()));
        bankAccountResponse.balance(bankAccount.getBalance());
        bankAccountResponse.clientId(String.valueOf(bankAccount.getClientId()));
        bankAccountResponse.currency(bankAccount.getCurrency().name());
        bankAccountResponse.type(bankAccount.getType().name());
        bankAccountResponse.openingDate(bankAccount.getOpeningDate().atOffset(ZoneOffset.UTC));
        bankAccountResponse.maintenance(bankAccount.getMaintenance());
        bankAccountResponse.limitMovements(bankAccount.getLimitMovements());
        bankAccountResponse.dayWithdrawalDeposit(String.valueOf(bankAccount.getDayWithdrawalDeposit()));

        if (bankAccount.getMovements() != null) {
            List<BankAccountResponseMovements> bankAccountResponseMovements = bankAccount.getMovements().stream()
                    .map(movement -> new BankAccountResponseMovements()
                            .amount(movement.getAmount())
                            .date(movement.getDate().atOffset(ZoneOffset.UTC))
                            .type(movement.getType()))
                    .collect(Collectors.toList());
            bankAccountResponse.movements(bankAccountResponseMovements);
        }

        if (bankAccount.getHolders() != null) {
            List<BankAccountResponseHolders> bankAccountResponseHolders = bankAccount.getHolders().stream()
                    .map(holder -> new BankAccountResponseHolders()
                            .name(holder.getName())
                            .document(holder.getDocument())
                            .documentType(holder.getDocumentType()))
                    .collect(Collectors.toList());
            bankAccountResponse.holders(bankAccountResponseHolders);
        }

        if (bankAccount.getAuthorizedSignatories() != null) {
            List<BankAccountResponseHolders> bankAccountResponseAutho = bankAccount.getAuthorizedSignatories().stream()
                    .map(holder -> new BankAccountResponseHolders()
                            .name(holder.getName())
                            .document(holder.getDocument())
                            .documentType(holder.getDocumentType()))
                    .collect(Collectors.toList());
            bankAccountResponse.authorizedSignatories(bankAccountResponseAutho);
        }

        return bankAccountResponse;
    }
}
