package com.example.User.Service.business;

import com.example.User.Service.domain.CreateUserRequest;
import com.example.User.Service.domain.CreateUserResponse;

public interface ICreateUser {
    CreateUserResponse createUser(CreateUserRequest userRequest);
}
