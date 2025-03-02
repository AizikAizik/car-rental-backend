package com.example.demo.controllers;

import com.example.demo.model.Booking;
import com.example.demo.model.BookingStatus;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.BookingService;
import jakarta.validation.Valid;
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

  // ✅ Create a new booking
  @PostMapping
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public ResponseEntity<?> createBooking(@RequestHeader("Authorization") String token, @Valid @RequestBody Booking booking) {
    String userEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
    Booking savedBooking = bookingService.createBooking(userEmail, booking.getCarId(), booking);
    return ResponseEntity.ok(savedBooking);
  }

  // ✅ Get all bookings for the logged-in user
  @GetMapping("/user")
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public ResponseEntity<List<Booking>> getUserBookings(@RequestHeader("Authorization") String token) {
    String userEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
    List<Booking> bookings = bookingService.getBookingsByUser(userEmail);
    return ResponseEntity.ok(bookings);
  }

  // ✅ Confirm a booking after successful payment
  @PutMapping("/{bookingId}/confirm")
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public ResponseEntity<Booking> confirmBooking(@PathVariable String bookingId) {
    Booking updatedBooking = bookingService.updateBookingStatus(bookingId, BookingStatus.CONFIRMED);
    return ResponseEntity.ok(updatedBooking);
  }

  // ✅ Cancel a booking
  @PutMapping("/{bookingId}/cancel")
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public ResponseEntity<Booking> cancelBooking(@PathVariable String bookingId) {
    Booking updatedBooking = bookingService.updateBookingStatus(bookingId, BookingStatus.CANCELLED);
    return ResponseEntity.ok(updatedBooking);
  }

  // ✅ Delete a booking (Only if user owns it)
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public ResponseEntity<Map<String, String>> deleteBooking(@PathVariable String id, @RequestHeader("Authorization") String token) {
    String userEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
    bookingService.deleteBooking(id, userEmail);
    return ResponseEntity.ok(Map.of("message", "Booking deleted successfully"));
  }
}

