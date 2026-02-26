package com.example.User.Service.business;

import com.example.User.Service.domain.LoginRequest;
import com.example.User.Service.domain.LoginResponse;

public interface ILogin {
    LoginResponse login(LoginRequest request);
}
