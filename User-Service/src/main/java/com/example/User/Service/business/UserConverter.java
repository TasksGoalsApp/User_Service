package com.example.User.Service.business;

import com.example.User.Service.domain.User;
import com.example.User.Service.repository.UserEntity;

public class UserConverter {
    public UserConverter(){}
    public static User convert(UserEntity userEntity){
        return User.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .name(userEntity.getName())
                .username(userEntity.getUsername())
                .dateofbirth(userEntity.getDateofbirth())
                .build();
    }
}
