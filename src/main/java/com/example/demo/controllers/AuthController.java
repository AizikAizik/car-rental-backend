package com.example.demo.controllers;

import com.example.demo.exception.AuthenticationException;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired
  private UserService userService;

  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody User user) {
    Map<String, Object> response = userService.registerUser(user);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody User loginRequest) {
    try{
      Map<String, Object> response = userService.authenticateUser(loginRequest);
      return ResponseEntity.ok(response);  // Return the response with status 200 OK
    } catch (AuthenticationException e){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

  }
}
