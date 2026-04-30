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
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@CrossOrigin(origins = "*")
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/user")
@RestController
public class UserController {
    @Autowired
    private ICreateUser createUser;
    @Autowired
    private IDeleteUser deleteUser;
    @Autowired
    private IGetUser getUser;
    @Autowired
    private IUpdateUserInfo updateUserInfo;
    @Autowired
    private ILogin login;


    @PostMapping("/register")
    @PermitAll
    public ResponseEntity<CreateUserResponse> createUser(@RequestBody CreateUserRequest createUserRequest) {
        CreateUserResponse createUserResponse = createUser.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createUserResponse);

    }



    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        deleteUser.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    @RolesAllowed({"Customer"})
    @GetMapping("{id}")
    public ResponseEntity<User> getUserById(@PathVariable(value = "id") final long userid){
        Optional<User> user = getUser.getUserById(userid);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/update")
    public ResponseEntity<UpdateUserInfoResponse> updateUserInfo(@RequestBody @Valid UpdateUserInfoRequest request){
        UpdateUserInfoResponse response = updateUserInfo.updateUserInfo(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @PermitAll
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = login.login(req);
        return ResponseEntity.ok(resp);
    }


}
