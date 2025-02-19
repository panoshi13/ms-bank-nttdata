package com.ntt.data.ms.client.repository;

import com.ntt.data.ms.client.entity.Customer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

    Mono<Customer> findByIdentification(String identification);
}
