package com.example.User.Service.business.Impl;

import com.example.User.Service.business.IUpdateUserInfo;
import com.example.User.Service.domain.UpdateUserInfoRequest;
import com.example.User.Service.domain.UpdateUserInfoResponse;
import com.example.User.Service.exception.ResourceNotFoundException;
import com.example.User.Service.repository.UserEntity;
import com.example.User.Service.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class UpdateUserInfoImpl implements IUpdateUserInfo {

    private final UserRepository userRepository;
    @Override
    public UpdateUserInfoResponse updateUserInfo(UpdateUserInfoRequest request, long userId) {
        Optional<UserEntity> userEntityUsername= userRepository.findByUsername(request.getUsername());
        if (userEntityUsername.isPresent() && !userEntityUsername.get().getId().equals(userId)) {

            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already in use");
        }
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        userEntity.setName(request.getName());
        userEntity.setDateofbirth(request.getDateOfBirth());
        userEntity.setEmail(request.getEmail());
        userEntity.setUsername(request.getUsername());

        UserEntity savedUser = userRepository.save(userEntity);
        return UpdateUserInfoResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .dateOfBirth(savedUser.getDateofbirth())
                .build();



    }
}
