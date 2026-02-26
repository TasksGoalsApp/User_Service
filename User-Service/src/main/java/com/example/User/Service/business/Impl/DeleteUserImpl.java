package com.example.User.Service.business.Impl;

import com.example.User.Service.business.IDeleteUser;
import com.example.User.Service.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeleteUserImpl implements IDeleteUser {
    private final UserRepository userRepository;
    public void deleteUser(long id){
        userRepository.deleteById(id);

    }
}
