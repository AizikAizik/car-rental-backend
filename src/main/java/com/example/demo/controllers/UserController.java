package com.example.demo.controllers;

import com.example.demo.model.User;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

  private final UserService userService;

  @Autowired
  private JwtUtil jwtUtil;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/profile")
  public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String token) {
    String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));

    User user = userService.getUserByEmail(email); // Fetch user from DB
    return ResponseEntity.ok(user);
  }
}
