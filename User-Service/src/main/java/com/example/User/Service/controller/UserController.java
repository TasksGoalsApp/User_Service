package com.example.User.Service.controller;

import com.example.User.Service.business.*;
import com.example.User.Service.domain.*;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@CrossOrigin(origins = "*")
@AllArgsConstructor
@RequestMapping("/user")
@RestController
public class UserController {

    private final ICreateUser createUser;

    private final IDeleteUser deleteUser;

    private final IGetUser getUser;

    private final IUpdateUserInfo updateUserInfo;

    private final ILogin login;


    @PostMapping("/register")
    @PermitAll
    public ResponseEntity<CreateUserResponse> createUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        CreateUserResponse createUserResponse = createUser.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createUserResponse);

    }


    @RolesAllowed({"CUSTOMER"})
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        deleteUser.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }


    @RolesAllowed({"CUSTOMER", "ADMIN"})
    @GetMapping("/me")
    public ResponseEntity<User> getUserById(@AuthenticationPrincipal Jwt jwt){
        Long userId = jwt.getClaim("userId");
        Optional<User> user = getUser.getUserById(userId);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    @RolesAllowed({"ADMIN"})
    @GetMapping("/admin/{userid}")
    public ResponseEntity<User> getUserById(  @PathVariable long userid){
        Optional<User> user = getUser.getUserById(userid);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }



    @RolesAllowed({"CUSTOMER", "ADMIN"})
    @PutMapping("/update")
    public ResponseEntity<UpdateUserInfoResponse> updateUserInfo(@RequestBody @Valid UpdateUserInfoRequest request, @AuthenticationPrincipal Jwt jwt){
        Long userId = jwt.getClaim("userId");
        UpdateUserInfoResponse response = updateUserInfo.updateUserInfo(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @PermitAll
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = login.login(req);
        return ResponseEntity.ok(resp);
    }


}
