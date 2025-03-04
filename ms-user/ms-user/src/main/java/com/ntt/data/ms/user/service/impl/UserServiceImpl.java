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

    @Override
    public Completable yanki(String phoneNumberOrigin, String phoneNumberDestiny, double amount) {
        userRepository.findByDocumentNumber(phoneNumberOrigin)
            .flatMap(user1 -> {
                if (user1.getBalance() < amount) {
                    log.info("User no tiene dinero suficiente");
                    return Maybe.empty();
                }
                user1.setBalance(user1.getBalance() - amount);
                return Maybe.just(userRepository.save(user1));
            });


        userRepository.findByDocumentNumber(phoneNumberDestiny)
            .flatMap(user -> {
                user.setBalance(user.getBalance() + amount);
                return Maybe.just(userRepository.save(user));
            });


        return Completable.complete();
    }

    public Maybe<User> findByDocumentNumber(String documentNumber) {
        log.info("Searching for user with document number: {}", documentNumber);
        return userRepository.findByDocumentNumber(documentNumber)
            .doOnSuccess(user -> log.info("User found: {}", user))
            .doOnComplete(() -> log.info("No user found for document number: {}", documentNumber))
            .doOnError(error -> log.error("Error finding user by document number: {}", error));
    }



}
