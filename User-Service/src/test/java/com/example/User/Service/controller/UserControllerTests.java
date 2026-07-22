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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;





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

}
