package com.ntt.data.ms.user.service;

import com.ntt.data.ms.user.entity.User;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;

import java.util.Optional;

public interface UserService {

    Flowable<User> getAllUsers();

    Completable createUser(User user);

    Completable updateUser(User user);

    Maybe<User> findByDocumentNumber(String documentNumber);

    Completable yanki(String phoneNumberOrigin, String phoneNumberDestiny, double amount);
}

