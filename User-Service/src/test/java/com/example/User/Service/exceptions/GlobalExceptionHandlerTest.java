package com.example.User.Service.exceptions;
import com.example.User.Service.exception.GlobalExceptionHandler;
import com.example.User.Service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnNotFoundResponse() {
        ResourceNotFoundException exception = new ResourceNotFoundException("User not found");

        ResponseEntity<?> response = handler.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertEquals("User not found", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void shouldReturnBadRequestForIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Email is already in use");

        ResponseEntity<?> response = handler.handleBadRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Email is already in use", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException() {
        Exception exception = new RuntimeException("Database failure");

        ResponseEntity<?> response = handler.handleGeneric(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        assertNotNull(response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("Something went wrong", body.get("message"));
        assertNotNull(body.get("timestamp"));

        assertNotEquals("Database failure", body.get("message"));
    }

    @Test
    void shouldReturnValidationErrors() throws Exception {
        TestRequest target = new TestRequest();

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "testRequest");

        bindingResult.addError(new FieldError(
                        "testRequest",
                        "username",
                        "Username is required"
                )
        );

        bindingResult.addError(new FieldError(
                        "testRequest",
                        "email",
                        "Email must be valid"
                )
        );

        Method method = TestController.class.getDeclaredMethod("testMethod", TestRequest.class);

        MethodParameter methodParameter = new MethodParameter(method, 0);

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                        methodParameter,
                        bindingResult
                );

        ResponseEntity<?> response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(400, body.get("status"));
        assertEquals("Validation Failed", body.get("error"));
        assertNotNull(body.get("timestamp"));

        Map<?, ?> messages = (Map<?, ?>) body.get("messages");

        assertEquals("Username is required", messages.get("username"));

        assertEquals("Email must be valid", messages.get("email"));
    }

    @Test
    void shouldKeepFirstValidationMessageForDuplicateField() throws Exception {

        TestRequest target = new TestRequest();

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
                        target,
                        "testRequest"
                );

        bindingResult.addError(new FieldError(
                        "testRequest",
                        "username",
                        "First message"
                )
        );

        bindingResult.addError(new FieldError(
                        "testRequest",
                        "username",
                        "Second message"
                )
        );

        Method method = TestController.class.getDeclaredMethod("testMethod", TestRequest.class);

        MethodParameter methodParameter = new MethodParameter(method, 0);

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<?> response = handler.handleValidation(exception);

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        Map<?, ?> messages = (Map<?, ?>) body.get("messages");

        assertEquals("First message", messages.get("username"));
    }

    private static class TestRequest {
        private String username;
        private String email;
    }

    private static class TestController {

        @SuppressWarnings("unused")
        void testMethod(TestRequest request) {
        }
    }

    @Test
    void shouldReturnUnauthorizedForBadCredentials() {
        BadCredentialsException exception = new BadCredentialsException("Invalid username or password");

        ResponseEntity<?> response = handler.handleBadCredentials(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(401, body.get("status"));
        assertEquals("Unauthorized", body.get("error"));
        assertEquals("Invalid username or password", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }


}
