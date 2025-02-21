package com.ntt.data.ms.credit.repository;

import com.ntt.data.ms.credit.entity.CreditType;
import com.ntt.data.ms.credit.entity.Credit;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditRepository extends ReactiveMongoRepository<Credit, String> {

    Mono<Long> countByClientIdAndType(ObjectId id, CreditType creditType);

    Flux<Credit> findByClientId(ObjectId clientId);
}

