package com.example.demo.services;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private PasswordEncoder passwordEncoder;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("John Doe", "john@example.com", "Password123!", "123 Main St", "+1234567890", Set.of(Role.USER));
  }

  @Test
  void registerUser_success() {
    when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(jwtUtil.generateToken("john@example.com")).thenReturn("mockToken");

    Map<String, Object> response = userService.registerUser(user);

    assertEquals("User registered successfully", response.get("message"));
    assertEquals("mockToken", response.get("token"));
    assertEquals(user, response.get("user"));
    assertEquals("hashedPassword", user.getPassword()); // Verify password was hashed
    verify(userRepository, times(1)).save(user);
    verify(jwtUtil, times(1)).generateToken("john@example.com");
  }

  @Test
  void authenticateUser_success() {
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("Password123!", user.getPassword())).thenReturn(true);
    when(jwtUtil.generateToken("john@example.com")).thenReturn("mockToken");

    User loginRequest = new User(null, "john@example.com", "Password123!", null, null, null);
    Map<String, Object> response = userService.authenticateUser(loginRequest);

    assertEquals("mockToken", response.get("token"));
    assertEquals(Optional.of(user), response.get("user"));
    verify(userRepository, times(1)).findByEmail("john@example.com");
    verify(passwordEncoder, times(1)).matches("Password123!", user.getPassword());
    verify(jwtUtil, times(1)).generateToken("john@example.com");
  }

  @Test
  void authenticateUser_userNotFound() {
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

    User loginRequest = new User(null, "john@example.com", "Password123!", null, null, null);
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      userService.authenticateUser(loginRequest);
    });

    assertEquals("User not found", exception.getMessage());
    verify(userRepository, times(1)).findByEmail("john@example.com");
    verify(passwordEncoder, never()).matches(anyString(), anyString());
    verify(jwtUtil, never()).generateToken(anyString());
  }

  @Test
  void authenticateUser_invalidPassword() {
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("WrongPassword", user.getPassword())).thenReturn(false);

    User loginRequest = new User(null, "john@example.com", "WrongPassword", null, null, null);
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      userService.authenticateUser(loginRequest);
    });

    assertEquals("Invalid email or password", exception.getMessage());
    verify(userRepository, times(1)).findByEmail("john@example.com");
    verify(passwordEncoder, times(1)).matches("WrongPassword", user.getPassword());
    verify(jwtUtil, never()).generateToken(anyString());
  }

  @Test
  void getUserByEmail_success() {
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

    User result = userService.getUserByEmail("john@example.com");

    assertEquals(user, result);
    verify(userRepository, times(1)).findByEmail("john@example.com");
  }

  @Test
  void getUserByEmail_notFound() {
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      userService.getUserByEmail("john@example.com");
    });

    assertEquals("User not found", exception.getMessage());
    verify(userRepository, times(1)).findByEmail("john@example.com");
  }
}
