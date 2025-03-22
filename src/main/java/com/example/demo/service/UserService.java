package com.example.demo.service;

import com.example.demo.exception.AuthenticationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private static final PolicyFactory SANITIZER = new HtmlPolicyBuilder()
          .allowWithoutAttributes() // Allow no elements with attributes
          .disallowElements("script", "div", "a", "img", "style", "p") // Disallow all HTML elements
          .disallowAttributes("on.*")
          .globally().toFactory();

  // Regex to remove <script> tags and extract their content
  private static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile("<script[^>]*>(.*?)</script>", Pattern.DOTALL);

  private String preprocessScriptTags(String input) {
    if (input == null) {
      return null;
    }
    Matcher matcher = SCRIPT_TAG_PATTERN.matcher(input);
    String result = input;
    while (matcher.find()) {
      String scriptContent = matcher.group(1); // Extract content inside <script> tags
      result = matcher.replaceFirst(scriptContent != null ? scriptContent : "");
      matcher = SCRIPT_TAG_PATTERN.matcher(result); // Reapply to handle multiple script tags
    }
    return result;
  }

  public Map<String, Object> registerUser(User user) {
    // Preprocess fields to handle <script> tags
    String preprocessedName = preprocessScriptTags(user.getName());
    String preprocessedAddress = preprocessScriptTags(user.getAddress());

    // Sanitize fields that could contain HTML
    user.setName(SANITIZER.sanitize(preprocessedName));
    user.setAddress(SANITIZER.sanitize(preprocessedAddress));

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

    throw new AuthenticationException("Invalid email or password");
  }

  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }
}
