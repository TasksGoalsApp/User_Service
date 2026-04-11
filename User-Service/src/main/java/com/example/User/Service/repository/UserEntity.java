package com.example.User.Service.repository;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotBlank
    @Length( max = 50)
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NotBlank
    @Length( max = 50)
    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @NotBlank
    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @NotNull
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateofbirth;

    @Column(name = "password", nullable = false, length = 150)
    @Length(max = 150)
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserRoleEntity role;
}
