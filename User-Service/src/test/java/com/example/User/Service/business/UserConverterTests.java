package com.example.User.Service.business;
import com.example.User.Service.domain.User;
import com.example.User.Service.repository.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;



public class UserConverterTests {
    @Test
    void shouldConvertUserEntityToUser() {
        LocalDate dateOfBirth = LocalDate.of(2000, 1, 1);

        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .name("Hristo Kolev")
                .username("hristo")
                .email("hristo@example.com")
                .dateofbirth(dateOfBirth)
                .password("encoded-password")
                .build();

        User result = UserConverter.convert(userEntity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Hristo Kolev", result.getName());
        assertEquals("hristo", result.getUsername());
        assertEquals("hristo@example.com", result.getEmail());
        assertEquals(dateOfBirth, result.getDateofbirth());
    }

    @Test
    void shouldConvertEntityWithNullOptionalFields() {
        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .username("hristo")
                .email("hristo@example.com")
                .password("encoded-password")
                .build();

        User result = UserConverter.convert(userEntity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("hristo", result.getUsername());
        assertEquals("hristo@example.com", result.getEmail());
        assertNull(result.getName());
        assertNull(result.getDateofbirth());
    }

    @Test
    void shouldNotExposePasswordDuringConversion() {
        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .name("Hristo Kolev")
                .username("hristo")
                .email("hristo@example.com")
                .dateofbirth(LocalDate.of(2000, 1, 1))
                .password("very-sensitive-encoded-password")
                .build();

        User result = UserConverter.convert(userEntity);

        assertNotNull(result);
        assertEquals("hristo", result.getUsername());
        assertEquals("hristo@example.com", result.getEmail());

        assertThrows(
                NoSuchFieldException.class,
                () -> User.class.getDeclaredField("password")
        );
    }
}
