package com.example.demo.controllers;

import com.example.demo.security.JwtUtil;
import com.example.demo.service.BookingService;
import com.example.demo.service.PaymentService;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

  private MockMvc mockMvc;

  @Mock
  private PaymentService paymentService;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private BookingService bookingService;

  @InjectMocks
  private PaymentController paymentController;

  private static final String TOKEN = "Bearer mock-token";
  private static final String USER_EMAIL = "user@example.com";

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    when(jwtUtil.extractEmail("mock-token")).thenReturn(USER_EMAIL);
  }

  @Test
  void createPaymentIntent_success() throws Exception {
    long amount = 50;
    String bookingId = "booking123";
    String clientSecret = "pi_123_secret_456";

    when(bookingService.isBookingAmountExact(bookingId, amount)).thenReturn(true);
    when(paymentService.createPaymentIntent(amount)).thenReturn(clientSecret);

    mockMvc.perform(post("/api/payments/create")
                    .header("Authorization", TOKEN)
                    .param("amount", String.valueOf(amount))
                    .param("bookingId", bookingId)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.clientSecret").value(clientSecret));

    verify(jwtUtil, times(1)).extractEmail("mock-token");
    verify(bookingService, times(1)).isBookingAmountExact(bookingId, amount);
    verify(paymentService, times(1)).createPaymentIntent(amount);
  }

  @Test
  void createPaymentIntent_invalidAmount() throws Exception {
    long amount = 50;
    String bookingId = "booking123";

    when(bookingService.isBookingAmountExact(bookingId, amount)).thenReturn(false);

    mockMvc.perform(post("/api/payments/create")
                    .header("Authorization", TOKEN)
                    .param("amount", String.valueOf(amount))
                    .param("bookingId", bookingId)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("The amount isn't valid. Please input the correct amount"));

    verify(jwtUtil, times(1)).extractEmail("mock-token");
    verify(bookingService, times(1)).isBookingAmountExact(bookingId, amount);
    verify(paymentService, never()).createPaymentIntent(anyLong());
  }

  @Test
  void createPaymentIntent_stripeException() throws Exception {
    long amount = 50;
    String bookingId = "booking123";

    when(bookingService.isBookingAmountExact(bookingId, amount)).thenReturn(true);
    when(paymentService.createPaymentIntent(amount))
            .thenThrow(new StripeException("Stripe error", "req_123", "error_code", 400) {
            });

    mockMvc.perform(post("/api/payments/create")
                    .header("Authorization", TOKEN)
                    .param("amount", String.valueOf(amount))
                    .param("bookingId", bookingId)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Stripe error; code: error_code; request-id: req_123"));

    verify(jwtUtil, times(1)).extractEmail("mock-token");
    verify(bookingService, times(1)).isBookingAmountExact(bookingId, amount);
    verify(paymentService, times(1)).createPaymentIntent(amount);
  }
}
