package com.ntt.data.ms.bank.accounts.repository;

import com.ntt.data.ms.bank.accounts.entity.BankAccount;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface BankAccountRepository extends ReactiveMongoRepository<BankAccount, String> {

    Flux<BankAccount> findByClientId(ObjectId clienteId);
}
