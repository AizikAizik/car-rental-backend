package com.example.demo.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "cars")
public class Car {
  @Id
  private String id;

  @NotBlank(message = "brand of car must be included")
  private String brand;

  @NotBlank(message = "model of car must be included")
  private String model;

  @NotBlank(message = "car type must be either AUTOMATIC | MANUAL")
  private String type;

  @NotBlank(message = "year make of car must be included")
  private int year;

  @NotBlank(message = "price per day of car must be included")
  @Min(value = 1)
  private double pricePerDay;

  @NotBlank(message = "availability of car must be included")
  private boolean available;

  private String imageUrl;

  public Car(String brand, String model, String type, int year, double pricePerDay, boolean available, String imageUrl) {
    this.brand = brand;
    this.model = model;
    this.type = type;
    this.year = year;
    this.pricePerDay = pricePerDay;
    this.available = available;
    this.imageUrl = imageUrl;
  }
}
