package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public Map<String, Object> registerUser(User user) {
    // Hash password and save user
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    userRepository.save(user);

    // Generate token
    String token = jwtUtil.generateToken(user.getEmail());

    // Prepare response
    Map<String, Object> response = new HashMap<>();
    response.put("message", "User registered successfully");
    response.put("token", token);  // Add token to response
    response.put("user", user);    // Add user object to response

    return response;
  }

  public Map<String, Object> authenticateUser(User loginRequest) {
    Optional<User> user = Optional.ofNullable(userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found")));

    if (user.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())) {
      String token = jwtUtil.generateToken(loginRequest.getEmail());

      Map<String, Object> response = new HashMap<>();
      response.put("token", token);
      response.put("user", user);
      return response;
    }

    throw new RuntimeException("Invalid email or password");
  }

  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }
}
