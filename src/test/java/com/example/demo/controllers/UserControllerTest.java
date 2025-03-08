package com.example.demo.controllers;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.security.JwtUtil;
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

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  private MockMvc mockMvc;

  @Mock
  private UserService userService;

  @Mock
  private JwtUtil jwtUtil;

  @InjectMocks
  private UserController userController;

  private static final String TOKEN = "Bearer mock-token";
  private static final String USER_EMAIL = "john@example.com";

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    when(jwtUtil.extractEmail("mock-token")).thenReturn(USER_EMAIL);
  }

  @Test
  void getUserProfile_success() throws Exception {
    User user = new User("John Doe", USER_EMAIL, "Password123!", "123 Main St", "+1234567890", Set.of(Role.USER));
    when(userService.getUserByEmail(USER_EMAIL)).thenReturn(user);

    mockMvc.perform(get("/api/user/profile")
                    .header("Authorization", TOKEN)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(USER_EMAIL))
            .andExpect(jsonPath("$.name").value("John Doe"));

    verify(jwtUtil, times(1)).extractEmail("mock-token");
    verify(userService, times(1)).getUserByEmail(USER_EMAIL);
  }

  @Test
  void getUserProfile_invalidToken() throws Exception {
    when(jwtUtil.extractEmail("mock-token")).thenThrow(new RuntimeException("Invalid token"));

    mockMvc.perform(get("/api/user/profile")
                    .header("Authorization", TOKEN)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid or missing token"));

    verify(jwtUtil, times(1)).extractEmail("mock-token");
    verify(userService, never()).getUserByEmail(anyString());
  }

}
