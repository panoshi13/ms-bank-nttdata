package com.ntt.data.ms.bank.accounts.mapper;

import com.ntt.data.ms.bank.accounts.entity.*;
import com.ntt.data.ms.bank.accounts.model.BankAccountResponse;
import com.ntt.data.ms.bank.accounts.model.BankAccountResponseHolders;
import com.ntt.data.ms.bank.accounts.model.BankAccountResponseMovements;
import com.ntt.data.ms.bank.accounts.model.InlineObject;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BankAccountMapper {
    public BankAccountResponse accountResponse(BankAccount bankAccount) {
        BankAccountResponse accountResponse = new BankAccountResponse();
        accountResponse.id(String.valueOf(bankAccount.getId()));
        accountResponse.type(bankAccount.getType().name());
        accountResponse.clientId(String.valueOf(bankAccount.getClientId()));
        accountResponse.balance(bankAccount.getBalance());
        accountResponse.currency(bankAccount.getCurrency().name());
        accountResponse.openingDate(bankAccount.getOpeningDate().atOffset(ZoneOffset.UTC));
        accountResponse.maintenance(bankAccount.getMaintenance());
        accountResponse.limitMovements(bankAccount.getLimitMovements());
        accountResponse.dayWithdrawalDeposit(String.valueOf(bankAccount.getDayWithdrawalDeposit()));

        accountResponse.setMovements(mapMovements(bankAccount.getMovements()));
        accountResponse.setHolders(mapHolders(bankAccount.getHolders()));
        accountResponse.setAuthorizedSignatories(
            mapSignatories(bankAccount.getAuthorizedSignatories()));

        return accountResponse;
    }

    private List<BankAccountResponseMovements> mapMovements(List<Movement> movements) {
        if (movements == null) return null;
        return movements.stream()
            .map(movement -> {
                BankAccountResponseMovements responseMovements = new BankAccountResponseMovements();
                responseMovements.setAmount(movement.getAmount());
                responseMovements.setType(movement.getType());
                responseMovements.setDate(movement.getDate().atOffset(ZoneOffset.UTC));
                return responseMovements;
            }).collect(Collectors.toList());
    }

    private List<BankAccountResponseHolders> mapHolders(List<Holder> holders) {
        if (holders == null) return null;
        return holders.stream()
            .map(holder -> {
                BankAccountResponseHolders responseHolders = new BankAccountResponseHolders();
                responseHolders.setDocument(holder.getDocument());
                responseHolders.setName(holder.getName());
                responseHolders.setDocumentType(holder.getDocumentType());
                return responseHolders;
            }).collect(Collectors.toList());
    }

    private List<BankAccountResponseHolders> mapSignatories(
        List<AuthorizedSignatories> authorizedSignatories) {
        if (authorizedSignatories == null) return null;
        return authorizedSignatories.stream()
            .map(accountsRegisterHolders -> {
                BankAccountResponseHolders responseHolders = new BankAccountResponseHolders();
                responseHolders.setDocument(accountsRegisterHolders.getDocument());
                responseHolders.setName(accountsRegisterHolders.getName());
                responseHolders.setDocumentType(accountsRegisterHolders.getDocumentType());
                return responseHolders;
            }).collect(Collectors.toList());
    }

    public BankAccount mapToBankAccount(InlineObject inlineObject) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setType(AccountType.valueOf(inlineObject.getType()));
        bankAccount.setClientId(new ObjectId(inlineObject.getClientId()));
        bankAccount.setCurrency(Currency.valueOf(inlineObject.getCurrency()));
        bankAccount.setBalance(inlineObject.getAmount());
        var holders = inlineObject.getHolders();
        var authored = inlineObject.getAuthorizedSignatories();
        List<Holder> holderList = null;
        if (holders != null) {
            holderList = holders.stream()
                .map(accountsRegisterHolders -> {
                    Holder holder = new Holder();
                    holder.setDocument(accountsRegisterHolders.getDocument());
                    holder.setName(accountsRegisterHolders.getName());
                    holder.setDocumentType(accountsRegisterHolders.getDocumentType());
                    return holder;
                }).collect(Collectors.toList());
        }
        bankAccount.setHolders(holderList);
        List<AuthorizedSignatories> signatoriesList = null;
        if (authored != null) {
            signatoriesList = authored.stream()
                .map(accountsRegisterHolders -> {
                    AuthorizedSignatories authorizedSignatories = new AuthorizedSignatories();
                    authorizedSignatories.setDocument(accountsRegisterHolders.getDocument());
                    authorizedSignatories.setName(accountsRegisterHolders.getName());
                    authorizedSignatories.setDocumentType(
                        accountsRegisterHolders.getDocumentType());
                    return authorizedSignatories;
                }).collect(Collectors.toList());
        }
        bankAccount.setAuthorizedSignatories(signatoriesList);
        return bankAccount;
    }

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
        bankAccountResponse.dayWithdrawalDeposit(
            String.valueOf(bankAccount.getDayWithdrawalDeposit()));

        if (bankAccount.getMovements() != null) {
            List<BankAccountResponseMovements> bankAccountResponseMovements =
                bankAccount.getMovements().stream()
                    .map(movement -> new BankAccountResponseMovements()
                        .amount(movement.getAmount())
                        .date(movement.getDate().atOffset(ZoneOffset.UTC))
                        .type(movement.getType()))
                    .collect(Collectors.toList());
            bankAccountResponse.movements(bankAccountResponseMovements);
        }

        if (bankAccount.getHolders() != null) {
            List<BankAccountResponseHolders> bankAccountResponseHolders =
                bankAccount.getHolders().stream()
                    .map(holder -> new BankAccountResponseHolders()
                        .name(holder.getName())
                        .document(holder.getDocument())
                        .documentType(holder.getDocumentType()))
                    .collect(Collectors.toList());
            bankAccountResponse.holders(bankAccountResponseHolders);
        }

        if (bankAccount.getAuthorizedSignatories() != null) {
            List<BankAccountResponseHolders> bankAccountResponseAutho =
                bankAccount.getAuthorizedSignatories().stream()
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

