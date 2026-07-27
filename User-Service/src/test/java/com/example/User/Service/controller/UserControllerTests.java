package com.example.User.Service.controller;
import com.example.User.Service.business.*;
import com.example.User.Service.domain.*;
import com.example.User.Service.filter.JwtAuthenticationFilter;
import com.example.User.Service.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;




@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ICreateUser createUser;

    @MockitoBean
    private IDeleteUser deleteUser;

    @MockitoBean
    private IGetUser getUser;

    @MockitoBean
    private IUpdateUserInfo updateUserInfo;

    @MockitoBean
    private ILogin login;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtUtil jwtUtil;
    @Autowired
    private UserController userController;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("Hristo Kolev")
                .username("hristo")
                .email("hristo@example.com")
                .password("Password123!")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .build();

        CreateUserResponse response = CreateUserResponse.builder()
                .id(1L)
                .build();

        when(createUser.createUser(any(CreateUserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/user/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L));

        verify(createUser).createUser(any(CreateUserRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenRegistrationRequestIsInvalid()
            throws Exception {

        CreateUserRequest request = CreateUserRequest.builder()
                .name("")
                .username("")
                .email("invalid-email")
                .password("")
                .dateOfBirth(null)
                .build();

        mockMvc.perform(post("/user/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createUser);
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsMalformed()
            throws Exception {

        mockMvc.perform(post("/user/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "hristo",
                                  "email": "hristo@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createUser);
    }

    //LOGIN FROM HERE

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("hristo")
                .password("Password123!")
                .build();

        LoginResponse response = LoginResponse.builder()
                .accessToken("valid-jwt-token")
                .build();

        when(login.login(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/user/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("valid-jwt-token"));

        verify(login).login(any(LoginRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenLoginRequestIsInvalid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("")
                .password("")
                .build();

        mockMvc.perform(post("/user/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(login);
    }
    @Test
    void shouldReturnBadRequestWhenLoginJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {
                              "username": "hristo",
                              "password":""
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(login);
    }

    //GET USER UNIT TESTS

    private Jwt createJwt(Long userId, String role) {
        Instant now = Instant.now();

        return new Jwt(
                "test-token",
                now,
                now.plusSeconds(3600),
                Map.of(
                        "alg", "none"
                ),
                Map.of(
                        "sub", "hristo",
                        "userId", userId,
                        "roles", java.util.List.of(role)
                )
        );
    }

    @Test
    void shouldReturnCurrentUserSuccessfully() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .name("Hristo Kolev")
                .username("hristo")
                .email("hristo@example.com")
                .dateofbirth(LocalDate.of(2000, 1, 1))
                .build();

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("userId")).thenReturn(userId);

        when(getUser.getUserById(userId)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.getUserById(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());
        assertEquals("Hristo Kolev", response.getBody().getName());
        assertEquals("hristo", response.getBody().getUsername());
        assertEquals("hristo@example.com", response.getBody().getEmail());

        verify(jwt).getClaim("userId");
        verify(getUser).getUserById(userId);
    }

    @Test
    void shouldReturnNotFoundWhenCurrentUserDoesNotExist() {
        Long userId = 999L;

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("userId")).thenReturn(userId);

        when(getUser.getUserById(userId)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.getUserById(jwt);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(jwt).getClaim("userId");
        verify(getUser).getUserById(userId);
    }


    // THIS IS THE DELETING USER TESTS!!!!!
    //

    @Test
    void shouldDeleteUserSuccessfully() {
        long userId = 1L;

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("userId")).thenReturn(userId);

        ResponseEntity<Void> response = userController.deleteUser(jwt);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(jwt).getClaim("userId");
        verify(deleteUser).deleteUser(userId);
    }

    // THIS IS THE UPDATING USER INTO TESTS !!!!!

    @Test
    void shouldUpdateUserSuccessfully() {

        long userId = 1L;

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("userId")).thenReturn(userId);

        UpdateUserInfoRequest request = UpdateUserInfoRequest.builder()
                        .name("New Name")
                        .username("newUsername")
                        .email("new@email.com")
                        .dateOfBirth(LocalDate.of(2000,1,1))
                        .build();

        UpdateUserInfoResponse response = UpdateUserInfoResponse.builder().build();

        when(updateUserInfo.updateUserInfo(request, userId)).thenReturn(response);

        ResponseEntity<UpdateUserInfoResponse> result = userController.updateUserInfo(request, jwt);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());

        verify(updateUserInfo).updateUserInfo(request, userId);
    }

    // ADMIN GETTING ALL THE USERS UNIT TESTS!!!!!!

    @Test
    void shouldReturnUserByIdForAdmin() {

        long userId = 5L;

        User user = User.builder()
                .id(userId)
                .name("Admin User")
                .username("admin")
                .email("admin@test.com")
                .build();

        when(getUser.getUserById(userId)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.getUserById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());

        verify(getUser).getUserById(userId);
    }

    @Test
    void shouldReturnNotFoundForAdminWhenUserDoesNotExist() {

        long userId = 5L;

        when(getUser.getUserById(userId)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.getUserById(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(getUser).getUserById(userId);
    }

}
