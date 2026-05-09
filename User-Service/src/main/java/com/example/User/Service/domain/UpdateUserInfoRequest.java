package com.example.User.Service.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserInfoRequest {

    @NotBlank
    private String name;
    @NotBlank
    private String username;
    @NotBlank
    private String email;
    @NotNull
    private LocalDate dateOfBirth;

}
