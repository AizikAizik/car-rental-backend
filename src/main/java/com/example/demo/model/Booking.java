package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.temporal.ChronoUnit;
import java.util.Date;

@Data
@Document(collection = "bookings")
public class Booking {
  @Id
  private String id;

  @DBRef
  private User user;

  @DBRef
  private Car car;

  @NotNull(message = "Start date is required")
  @Future(message = "Start date must be in the future")
  private Date startDate;

  @NotNull(message = "End date is required")
  @Future(message = "End date must be in the future")
  private Date endDate;

  @NotNull
  private BookingStatus status = BookingStatus.PENDING; // ✅ Default status is PENDING

  private double priceOfBooking;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @NotBlank
  private String carId;

  // 🔹 Calculate price before saving
  public void calculatePrice() {
    long daysBetween = ChronoUnit.DAYS.between(startDate.toInstant(), endDate.toInstant());
    this.priceOfBooking = daysBetween * car.getPricePerDay();
  }
}

