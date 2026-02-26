package com.example.User.Service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserInfoResponse {
    private long id;
    private String name;
    private String username;
    private LocalDate dateOfBirth;
    private String email;
}
