package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "users")
public class User {

  // ✅ Use @PersistenceCreator for MongoDB
  @PersistenceCreator
  public User(String name, String email, String password, String address, String phoneNumber, Set<Role> roles) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.phoneNumber = phoneNumber;
    this.address = address;
    this.roles = (roles == null || roles.isEmpty()) ? new HashSet<Role>(Set.of(Role.USER)) : roles;
  }

  @Id
  private String id;

  @NotBlank(message = "Full name is required")
  private String name;

  @Email
  @UniqueElements
  @NotBlank(message = "Email is required")
  private String email;

  @NotBlank(message = "Address is required")
  private String address;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @NotBlank(message = "Password is required")
  @Pattern(
          regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
          message = "Password must be at least 8 characters long, contain at least one letter, one number, and one special character"
  )
  private String password;

  @Pattern(
          regexp = "^\\+?[1-9]\\d{1,14}$",
          message = "Invalid phone number format"
  )
  private String phoneNumber;

  private Set<Role> roles;

}
