package com.example.demo.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

  @Value("${stripe.secret.key}")
  private String stripeSecretKey;

  public String createPaymentIntent(long amount) throws StripeException {
    Stripe.apiKey = stripeSecretKey;

    PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amount * 100) // Amount in cents
            .setCurrency("usd")
            .build();

    PaymentIntent paymentIntent = PaymentIntent.create(params);
    return paymentIntent.getClientSecret();
  }
}
