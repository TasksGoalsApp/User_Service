package com.example.User.Service.business.Impl;

import org.jetbrains.annotations.NotNull;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String msg) {
        super(msg);
    }
}
