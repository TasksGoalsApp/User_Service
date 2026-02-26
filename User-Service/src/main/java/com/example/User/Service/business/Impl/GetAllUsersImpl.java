package com.example.User.Service.business.Impl;

import com.example.User.Service.business.IGetAllUsers;
import com.example.User.Service.business.UserConverter;
import com.example.User.Service.domain.GetAllUsersResponse;
import com.example.User.Service.domain.User;
import com.example.User.Service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GetAllUsersImpl implements IGetAllUsers {
    private UserRepository userRepository;

    @Override
    @Transactional
    public GetAllUsersResponse getAllUsers(){
        List<User> users;
        users = userRepository.findAll()
                .stream()
                .map(UserConverter::convert)
                .toList();

        return GetAllUsersResponse.builder()
                .users(users)
                .build();


    }

}
