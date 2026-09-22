package com.example.User.Service;
import com.example.User.Service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class LoginFlowTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JwtDecoder decoder;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;

    @Test void registrationLoginAndProfileUseTheSameIdentityAndRole() throws Exception {
        mvc.perform(post("/user/register").contentType("application/json").content("""
                {"name":"Test User","username":"tester","email":"tester@example.com","password":"test-password","dateOfBirth":"2000-01-01"}
                """)).andExpect(status().isCreated());
        var user = users.findByUsername("tester").orElseThrow();
        assertThat(user.getPassword()).isNotEqualTo("test-password");
        assertThat(passwords.matches("test-password",user.getPassword())).isTrue();
        var result = mvc.perform(post("/user/login").contentType("application/json").content("""
                {"username":"tester","password":"test-password"}
                """)).andExpect(status().isOk()).andExpect(jsonPath("$.expiresIn").value(3600)).andReturn();
        var token = mapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
        var jwt = decoder.decode(token);
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("CUSTOMER");
        assertThat(((Number)jwt.getClaims().get("id")).longValue()).isEqualTo(user.getId());
        mvc.perform(get("/user/me").header("Authorization","Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.password").doesNotExist()).andExpect(header().doesNotExist("Set-Cookie"));
        mvc.perform(get("/user/admin/" + user.getId()).header("Authorization","Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/user/admin/" + user.getId()).header("Authorization","Bearer " + TestTokens.token(user.getId(),"ADMIN")))
                .andExpect(status().isOk());
        mvc.perform(post("/user/login").contentType("application/json").content("""
                {"username":"tester","password":"incorrect"}
                """)).andExpect(status().isUnauthorized());
    }
    @Test void unknownUserReturnsUnauthorized() throws Exception {
        mvc.perform(post("/user/login").contentType("application/json").content("""
                {"username":"missing-user","password":"incorrect"}
                """)).andExpect(status().isUnauthorized());
    }
}
