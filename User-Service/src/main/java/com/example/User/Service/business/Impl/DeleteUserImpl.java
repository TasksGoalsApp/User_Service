package com.example.User.Service.business.Impl;

import com.example.User.Service.business.IDeleteUser;
import com.example.User.Service.exception.ResourceNotFoundException;
import com.example.User.Service.repository.UserEntity;
import com.example.User.Service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeleteUserImpl implements IDeleteUser {
    private final UserRepository userRepository;
    @Transactional
    @Override
    public void deleteUser(long id){
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        userRepository.delete(user);

    }
}
