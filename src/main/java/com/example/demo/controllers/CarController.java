package com.example.demo.controllers;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Car;
import com.example.demo.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {
  @Autowired
  private CarRepository carRepository;

  @GetMapping
  public List<Car> getAllCars() {
    return carRepository.findAll();
  }

  @GetMapping("/{carId}")
  public ResponseEntity<Car> getSpecificCar(@PathVariable String carId){
    Car car = carRepository.findById(carId).orElseThrow( () -> new ResourceNotFoundException("car with that Id not found"));

    return ResponseEntity.ok(car);
  }

  @PostMapping
  public Car createCar(@RequestBody Car car) {
    return carRepository.save(car);
  }
}
