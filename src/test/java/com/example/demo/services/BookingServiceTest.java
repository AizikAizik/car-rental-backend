package com.example.demo.services;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.CarRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

  @InjectMocks
  private BookingService bookingService;

  @Mock
  private BookingRepository bookingRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CarRepository carRepository;

  private User user;
  private Car car;
  private Booking booking;

  @BeforeEach
  void setUp() {
    user = new User("John Doe", "john@gmail.com", "password123$", "Nottingham", "+23058496844", Collections.singleton(Role.USER));

    car = new Car("Tesla", "Cyber Truck", "AUTOMATIC", 2023, 30.0, true, "");
    car.setId("car123");

    booking = new Booking();
    booking.setCarId("car123");
    booking.setStartDate(new Date());
    booking.setEndDate(new Date(System.currentTimeMillis() + 86400000)); // +1 day
  }

  @Test
  void createBooking_success() {
    when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
    when(carRepository.findById("car123")).thenReturn(Optional.of(car));
    when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

    Booking result = bookingService.createBooking("john@gmail.com", "car123", booking);

    assertEquals(BookingStatus.PENDING, result.getStatus());
    assertEquals(user, result.getUser());
    assertEquals(car, result.getCar());
    verify(bookingRepository, times(1)).save(booking);
  }

  @Test
  void createBooking_userNotFound() {
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      bookingService.createBooking("user@example.com", "car123", booking);
    });

    assertEquals("User not found", exception.getMessage());
    verify(carRepository, never()).findById(anyString());
    verify(bookingRepository, never()).save(any(Booking.class));
  }

  @Test
  void createBooking_carNotFound() {
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(carRepository.findById("car123")).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      bookingService.createBooking("user@example.com", "car123", booking);
    });

    assertEquals("Car not found", exception.getMessage());
    verify(bookingRepository, never()).save(any(Booking.class));
  }

  @Test
  void createBooking_invalidDates() {
    booking.setEndDate(new Date(System.currentTimeMillis() - 86400000)); // End before start

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(carRepository.findById("car123")).thenReturn(Optional.of(car));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      bookingService.createBooking("user@example.com", "car123", booking);
    });

    assertEquals("End date must be after start date", exception.getMessage());
    verify(bookingRepository, never()).save(any(Booking.class));
  }

  @Test
  void updateBookingStatus_success() {
    when(bookingRepository.findById("booking123")).thenReturn(Optional.of(booking));
    when(bookingRepository.save(booking)).thenReturn(booking);

    Booking result = bookingService.updateBookingStatus("booking123", BookingStatus.CONFIRMED);

    assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    verify(bookingRepository, times(1)).save(booking);
  }

  @Test
  void updateBookingStatus_bookingNotFound() {
    when(bookingRepository.findById("booking123")).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      bookingService.updateBookingStatus("booking123", BookingStatus.CONFIRMED);
    });

    assertEquals("Booking not found", exception.getMessage());
    verify(bookingRepository, never()).save(any(Booking.class));
  }

  @Test
  void getBookingsByUser_success() {
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(bookingRepository.findByUser(user)).thenReturn(List.of(booking));

    List<Booking> result = bookingService.getBookingsByUser("user@example.com");

    assertEquals(1, result.size());
    assertEquals(booking, result.get(0));
    verify(bookingRepository, times(1)).findByUser(user);
  }

  @Test
  void getBookingsByUser_userNotFound() {
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      bookingService.getBookingsByUser("user@example.com");
    });

    assertEquals("User not found", exception.getMessage());
    verify(bookingRepository, never()).findByUser(any(User.class));
  }

  @Test
  void deleteBooking_success() {
    user.setEmail("user@example.com");
    booking.setUser(user);
    when(bookingRepository.findById("booking123")).thenReturn(Optional.of(booking));

    bookingService.deleteBooking("booking123", "user@example.com");

    verify(bookingRepository, times(1)).deleteById("booking123");
  }

  @Test
  void deleteBooking_unauthorized() {
    user.setEmail("other@example.com");
    booking.setUser(user);
    when(bookingRepository.findById("booking123")).thenReturn(Optional.of(booking));

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      bookingService.deleteBooking("booking123", "user@example.com");
    });

    assertEquals("Unauthorized to delete this booking", exception.getMessage());
    verify(bookingRepository, never()).deleteById(anyString());
  }

  @Test
  void deleteBooking_bookingNotFound() {
    when(bookingRepository.findById("booking123")).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      bookingService.deleteBooking("booking123", "user@example.com");
    });

    assertEquals("Booking not found", exception.getMessage());
    verify(bookingRepository, never()).deleteById(anyString());
  }

  @Test
  void isBookingAmountExact_success() {
    booking.setPriceOfBooking(50.0);
    when(bookingRepository.findById("booking123")).thenReturn(Optional.of(booking));

    boolean result = bookingService.isBookingAmountExact("booking123", 50);

    assertTrue(result);
    verify(bookingRepository, times(1)).findById("booking123");
  }

  @Test
  void isBookingAmountExact_invalidAmount() {
    booking.setPriceOfBooking(50.0);
    when(bookingRepository.findById("booking123")).thenReturn(Optional.of(booking));

    boolean result = bookingService.isBookingAmountExact("booking123", 60);

    assertFalse(result);
    verify(bookingRepository, times(1)).findById("booking123");
  }

  @Test
  void isBookingAmountExact_bookingNotFound() {
    when(bookingRepository.findById("booking123")).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
      bookingService.isBookingAmountExact("booking123", 50);
    });

    assertEquals("booking Id:booking123 not found", exception.getMessage());
  }
}
