package com.example.demo.controllers;

import com.example.demo.model.Booking;
import com.example.demo.model.BookingStatus;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

  private final BookingService bookingService;
  private final JwtUtil jwtUtil;

  public BookingController(BookingService bookingService, JwtUtil jwtUtil) {
    this.bookingService = bookingService;
    this.jwtUtil = jwtUtil;
  }

  private String extractEmailFromToken(String token) {
    if (token == null || !token.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Invalid or missing Authorization header");
    }
    try {
      return jwtUtil.extractEmail(token.replace("Bearer ", ""));
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Invalid token: " + e.getMessage());
    }
  }

  @PostMapping
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public ResponseEntity<?> createBooking(@RequestHeader("Authorization") String token, @Valid @RequestBody Booking booking) {
    try {
      String userEmail = extractEmailFromToken(token);
      Booking savedBooking = bookingService.createBooking(userEmail, booking.getCarId(), booking);
      return ResponseEntity.ok(savedBooking);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/user")
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public ResponseEntity<?> getUserBookings(@RequestHeader("Authorization") String token) {
    try {
      String userEmail = extractEmailFromToken(token);
      List<Booking> bookings = bookingService.getBookingsByUser(userEmail);
      return ResponseEntity.ok(bookings);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("/{bookingId}/confirm")
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public ResponseEntity<?> confirmBooking(@PathVariable String bookingId, @RequestHeader("Authorization") String token) {
    try {
      extractEmailFromToken(token); // Validate token, even if not used directly
      Booking updatedBooking = bookingService.updateBookingStatus(bookingId, BookingStatus.CONFIRMED);
      return ResponseEntity.ok(updatedBooking);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("/{bookingId}/cancel")
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public ResponseEntity<?> cancelBooking(@PathVariable String bookingId, @RequestHeader("Authorization") String token) {
    try {
      extractEmailFromToken(token); // Validate token, even if not used directly
      Booking updatedBooking = bookingService.updateBookingStatus(bookingId, BookingStatus.CANCELLED);
      return ResponseEntity.ok(updatedBooking);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public ResponseEntity<?> deleteBooking(@PathVariable String id, @RequestHeader("Authorization") String token) {
    try {
      String userEmail = extractEmailFromToken(token);
      bookingService.deleteBooking(id, userEmail);
      return ResponseEntity.ok(Map.of("message", "Booking deleted successfully"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }
  }
}