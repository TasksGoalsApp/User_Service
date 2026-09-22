package com.example.User.Service.business.Impl;

import com.example.User.Service.business.ILogin;
import com.example.User.Service.domain.LoginRequest;
import com.example.User.Service.domain.LoginResponse;
import com.example.User.Service.repository.UserRepository;
import com.example.User.Service.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LoginImpl implements ILogin {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Authenticate once through the configured BCrypt-backed provider.
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                request.getUsername(), request.getPassword()));
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
        if (user.getRole() == null || user.getRole().getRole() == null) {
            throw new BadCredentialsException("Bad credentials");
        }
        return LoginResponse.builder()
                .accessToken(jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().getRole().name()))
                .tokenType("Bearer").expiresIn(jwtUtil.getExpiresInSeconds()).build();
    }
}
