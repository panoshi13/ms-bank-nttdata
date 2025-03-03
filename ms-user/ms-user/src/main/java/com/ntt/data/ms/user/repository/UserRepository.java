package com.ntt.data.ms.user.repository;

import com.ntt.data.ms.user.entity.User;
import io.reactivex.rxjava3.core.Maybe;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Maybe<User> findByDocumentNumber(String documentNumber);

}
