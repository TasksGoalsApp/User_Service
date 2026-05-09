package com.example.User.Service.business.Impl;

import com.example.User.Service.business.IGetUser;
import com.example.User.Service.business.UserConverter;
import com.example.User.Service.domain.User;
import com.example.User.Service.exception.ResourceNotFoundException;
import com.example.User.Service.repository.UserEntity;
import com.example.User.Service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
@AllArgsConstructor
@Service
public class GetUserImpl implements IGetUser {
    private final UserRepository userRepository;
    @Transactional
    @Override
    public Optional<User> getUserById(long id) {
        User user = userRepository.findById(id).map(UserConverter::convert)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return Optional.of(user);
    }
}
