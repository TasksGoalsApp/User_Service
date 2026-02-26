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
    @Column(name = "Name")
    private String name;

    @NotBlank
    @Length( max = 50)
    @Column(name = "Username")
    private String username;

    @NotBlank
    @Column(name = "Email")
    private String email;

    @NotNull
    @Column(name = "Date")
    private LocalDate dateofbirth;

    @Column(name = "Password")
    @Length(max = 150)
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserRoleEntity role;
}
