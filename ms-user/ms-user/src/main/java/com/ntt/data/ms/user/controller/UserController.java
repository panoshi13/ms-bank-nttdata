package com.ntt.data.ms.user.controller;


import com.ntt.data.ms.user.entity.User;
import com.ntt.data.ms.user.service.UserService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Flowable<User>> getAllUsers() {
        Flowable<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping(value = "/register")
    public Completable createUser(@RequestBody User user) {
        return userService.createUser(user)
            .doOnComplete(() -> {
                ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.singletonMap("message", "usuario registrado exitosamente"));
            });
    }

    @PutMapping("/{id}")
    public Completable updateUser(@PathVariable String id, @RequestBody User user) {
        user.setId(new ObjectId(id));
        return userService.updateUser(user);
    }
}
