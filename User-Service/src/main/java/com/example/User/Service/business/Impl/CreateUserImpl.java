package com.example.User.Service.business.Impl;

import com.example.User.Service.business.ICreateUser;
import com.example.User.Service.domain.CreateUserRequest;
import com.example.User.Service.domain.CreateUserResponse;
import com.example.User.Service.repository.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CreateUserImpl implements ICreateUser {
    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public CreateUserResponse createUser(CreateUserRequest userRequest) {

        if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + userRequest.getUsername() + "' is already in use");
        }
        if(userRepository.existsByEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }



        UserEntity userEntity = saveNewUser(userRequest);
        return CreateUserResponse.builder()
                .id(userEntity.getId())
                .build();

    }

    private UserEntity saveNewUser(CreateUserRequest userRequest){
        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());

        UserEntity user = UserEntity.builder()
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .name(userRequest.getName())
                .password(encodedPassword)
                .dateofbirth(userRequest.getDateOfBirth())
                .build();

        UserRoleEntity role = UserRoleEntity.builder()
                .role(Role.CUSTOMER)   // or Role.USER
                .user(user)            // link back
                .build();

        user.setRole(role);
        return userRepository.save(user);
    }

}
