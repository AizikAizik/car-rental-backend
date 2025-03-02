package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Booking;
import com.example.demo.model.BookingStatus;
import com.example.demo.model.Car;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.CarRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BookingService {
  private final BookingRepository bookingRepository;
  private final UserRepository userRepository;
  private final CarRepository carRepository;

  public BookingService(BookingRepository bookingRepository, UserRepository userRepository, CarRepository carRepository) {
    this.bookingRepository = bookingRepository;
    this.userRepository = userRepository;
    this.carRepository = carRepository;
  }

  public Booking createBooking(String userId, String carId, Booking booking) {
    User user = userRepository.findByEmail(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    Car car = carRepository.findById(carId).orElseThrow(() -> new ResourceNotFoundException("Car not found"));

    // 🔹 Validate that start date is before end date
    if (!booking.getEndDate().after(booking.getStartDate())) {
      throw new IllegalArgumentException("End date must be after start date");
    }

    booking.setUser(user);
    booking.setCar(car);
    booking.setStatus(BookingStatus.PENDING);

    // 🔹 Calculate booking price before saving
    booking.calculatePrice();

    return bookingRepository.save(booking);
  }

  public Booking updateBookingStatus(String bookingId, BookingStatus newStatus) {
    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

    booking.setStatus(newStatus);
    return bookingRepository.save(booking);
  }

  public List<Booking> getBookingsByUser(String userEmail) {
    User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return bookingRepository.findByUser(user);
  }

  public void deleteBooking(String bookingId, String userEmail) {
    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

    // Ensure only the owner can delete
    if (!booking.getUser().getEmail().equals(userEmail)) {
      throw new RuntimeException("Unauthorized to delete this booking");
    }

    bookingRepository.deleteById(bookingId);
  }

  public List<Booking> getExpiredBookings() {
    return bookingRepository.findByEndDateBefore(new Date());
  }

  public List<Booking> getPendingBookings() {
    return bookingRepository.findByStatus(BookingStatus.PENDING);
  }

  public List<Booking> getBookingsBetween(Date from, Date to) {
    return bookingRepository.findByStartDateBetween(from, to);
  }

}

