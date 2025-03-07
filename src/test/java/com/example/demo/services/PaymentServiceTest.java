package com.example.demo.services;

import com.example.demo.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @InjectMocks
  private PaymentService paymentService;

  @Mock
  private PaymentIntent paymentIntent;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    // Use reflection to set the private stripeSecretKey field
    Field stripeSecretKeyField = PaymentService.class.getDeclaredField("stripeSecretKey");
    stripeSecretKeyField.setAccessible(true);
    stripeSecretKeyField.set(paymentService, "sk_test_fake_key");
  }

  @Test
  void createPaymentIntent_success() throws StripeException {
    try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
      // Arrange
      long amount = 50;
      String clientSecret = "pi_123_secret_456";
      when(PaymentIntent.create(any(PaymentIntentCreateParams.class))).thenReturn(paymentIntent);
      when(paymentIntent.getClientSecret()).thenReturn(clientSecret);

      // Act
      String result = paymentService.createPaymentIntent(amount);

      // Assert
      assertEquals(clientSecret, result);
      mockedStatic.verify(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)), times(1));
    }
  }

  @Test
  void createPaymentIntent_stripeException() throws StripeException {
    try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
      // Arrange
      long amount = 50;
      when(PaymentIntent.create(any(PaymentIntentCreateParams.class)))
              .thenThrow(new StripeException("Stripe error", "req_123", "error_code", 400) {});

      // Act & Assert
      StripeException exception = assertThrows(StripeException.class, () -> {
        paymentService.createPaymentIntent(amount);
      });
      assertEquals("Stripe error; code: error_code; request-id: req_123", exception.getMessage());
      mockedStatic.verify(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)), times(1));
    }
  }
}