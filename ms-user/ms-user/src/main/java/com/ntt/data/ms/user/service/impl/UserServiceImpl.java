package com.ntt.data.ms.user.service.impl;

import com.ntt.data.ms.user.entity.User;
import com.ntt.data.ms.user.repository.UserRepository;
import com.ntt.data.ms.user.service.UserService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Flowable<User> getAllUsers() {
        return Flowable.fromPublisher(userRepository.findAll());
    }

    @Override
    public Completable createUser(User user) {
        return Completable.fromPublisher(userRepository.save(user));
    }

    @Override
    public Completable updateUser(User user) {
        return Completable.fromPublisher(userRepository.save(user));
    }

    public Maybe<User> findByDocumentNumber(String documentNumber) {
        log.info("Searching for user with document number: {}", documentNumber);
        return userRepository.findByDocumentNumber(documentNumber)
            .doOnSuccess(user -> log.info("User found: {}", user))
            .doOnComplete(() -> log.info("No user found for document number: {}", documentNumber))
            .doOnError(error -> log.error("Error finding user by document number: {}", error));
    }

}
