package com.example.demo.controllers;

import com.example.demo.exception.AuthenticationException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  private MockMvc mockMvc;

  @Mock
  private UserService userService;

  @InjectMocks
  private AuthController authController;

  private User user;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    user = new User("John Doe", "john@example.com", "Password123!", "123 Main St", "+1234567890", Set.of(Role.USER));
  }

  @Test
  void registerUser_success() throws Exception {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "User registered successfully");
    response.put("token", "mockToken");
    response.put("user", user);
    when(userService.registerUser(any(User.class))).thenReturn(response);

    mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"Password123!\",\"address\":\"123 Main St\",\"phoneNumber\":\"+1234567890\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("User registered successfully"))
            .andExpect(jsonPath("$.token").value("mockToken"));

    verify(userService, times(1)).registerUser(any(User.class));
  }

  @Test
  void login_success() throws Exception {
    Map<String, Object> response = new HashMap<>();
    response.put("token", "mockToken");
    response.put("user", user);
    when(userService.authenticateUser(any(User.class))).thenReturn(response);

    mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"john@example.com\",\"password\":\"Password123!\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("mockToken"));

    verify(userService, times(1)).authenticateUser(any(User.class));
  }

  @Test
  void login_invalidCredentials() throws Exception {
    when(userService.authenticateUser(any(User.class))).thenThrow(new AuthenticationException("Invalid email or password"));

    mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"john@example.com\",\"password\":\"WrongPassword\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid email or password"));

    verify(userService, times(1)).authenticateUser(any(User.class));
  }
}
