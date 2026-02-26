package com.example.User.Service.business;

import com.example.User.Service.domain.User;

import java.util.Optional;

public interface IGetUser {
    Optional<User> getUserById(long id);
}
