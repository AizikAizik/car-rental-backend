package com.example.demo.controllers;

import com.example.demo.security.JwtUtil;
import com.example.demo.service.BookingService;
import com.example.demo.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Slf4j
public class PaymentController {

  private final BookingService bookingService;
  private final PaymentService paymentService;
  private final JwtUtil jwtUtil;

  public PaymentController(PaymentService paymentService, BookingService bookingService, JwtUtil jwtUtil) {
    this.paymentService = paymentService;
    this.jwtUtil = jwtUtil;
    this.bookingService = bookingService;
  }

  @PostMapping("/create")
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public ResponseEntity<Map<String, String>> createPaymentIntent(
          @RequestParam("amount") long amount,
          @RequestParam("bookingId") String bookingId,
          @RequestHeader("Authorization") String token) {
    try {
      String userEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
      log.info("{} is about to make payment", userEmail);

      if (!bookingService.isBookingAmountExact(bookingId, amount)){
        return ResponseEntity.badRequest().body(Map.of("error", "The amount isn't valid. Please input the correct amount"));
      }

      String clientSecret = paymentService.createPaymentIntent(amount);
      Map<String, String> response = new HashMap<>();
      response.put("clientSecret", clientSecret);
      log.info("{} has successfully paid: ${}", userEmail, amount);
      return ResponseEntity.ok(response);
    } catch (StripeException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }
}
