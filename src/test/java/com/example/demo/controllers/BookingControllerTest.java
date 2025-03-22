package com.example.demo.controllers;

import com.example.demo.model.Booking;
import com.example.demo.model.BookingStatus;
import com.example.demo.repository.CarRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

  private MockMvc mockMvc;

  @Mock
  private BookingService bookingService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CarRepository carRepository;

  @Mock
  private JwtUtil jwtUtil;

  @InjectMocks
  private BookingController bookingController;

  private static final String TOKEN = "Bearer mock-token";
  private static final String USER_EMAIL = "user@example.com";

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
    when(jwtUtil.extractEmail("mock-token")).thenReturn(USER_EMAIL);
  }

//  @Test
//  void createBooking_success() throws Exception {
//    Booking booking = new Booking();
//    booking.setCarId("car123");
//    booking.setStartDate(new Date());
//    booking.setEndDate(new Date(System.currentTimeMillis() + 86400000));
//    when(bookingService.createBooking(eq(USER_EMAIL), eq("car123"), any(Booking.class))).thenReturn(booking);
//
//    mockMvc.perform(post("/api/bookings")
//                    .header("Authorization", TOKEN)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content("{\"carId\":\"car123\",\"startDate\":\"2025-03-22\",\"endDate\":\"2025-03-23\"}"))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.status").value("PENDING"));
//
//    verify(jwtUtil, times(1)).extractEmail("mock-token");
//    verify(bookingService, times(1)).createBooking(eq(USER_EMAIL), eq("car123"), any(Booking.class));
//  }

//  @Test
//  void createBooking_invalidToken() throws Exception {
//    when(jwtUtil.extractEmail("mock-token")).thenThrow(new RuntimeException("Invalid token"));
//
//    mockMvc.perform(post("/api/bookings")
//                    .header("Authorization", TOKEN)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content("{\"carId\":\"car123\",\"startDate\":\"2025-03-09\",\"endDate\":\"2025-03-10\"}"))
//            .andExpect(status().isBadRequest());
//
//    verify(jwtUtil, times(1)).extractEmail("mock-token");
//    verify(bookingService, never()).createBooking(anyString(), anyString(), any(Booking.class));
//  }

  @Test
  void getUserBookings_success() throws Exception {
    Booking booking = new Booking();
    when(bookingService.getBookingsByUser(USER_EMAIL)).thenReturn(Collections.singletonList(booking));

    mockMvc.perform(get("/api/bookings/user")
                    .header("Authorization", TOKEN)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").exists());

    verify(jwtUtil, times(1)).extractEmail("mock-token");
    verify(bookingService, times(1)).getBookingsByUser(USER_EMAIL);
  }

  @Test
  void getUserBookings_invalidToken() throws Exception {
    when(jwtUtil.extractEmail("mock-token")).thenThrow(new RuntimeException("Invalid token"));

    mockMvc.perform(get("/api/bookings/user")
                    .header("Authorization", TOKEN)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid token: Invalid token"));

    verify(jwtUtil, times(1)).extractEmail("mock-token");
    verify(bookingService, never()).getBookingsByUser(anyString());
  }

  @Test
  void confirmBooking_success() throws Exception {
    Booking booking = new Booking();
    booking.setStatus(BookingStatus.CONFIRMED);
    when(bookingService.updateBookingStatus("booking123", BookingStatus.CONFIRMED)).thenReturn(booking);

    mockMvc.perform(put("/api/bookings/booking123/confirm")
                    .header("Authorization", TOKEN)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CONFIRMED"));

    verify(jwtUtil, times(1)).extractEmail("mock-token");
    verify(bookingService, times(1)).updateBookingStatus("booking123", BookingStatus.CONFIRMED);
  }

  @Test
  void confirmBooking_invalidToken() throws Exception {
    when(jwtUtil.extractEmail("mock-token")).thenThrow(new RuntimeException("Invalid token"));

    mockMvc.perform(put("/api/bookings/booking123/confirm")
                    .header("Authorization", TOKEN)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid token: Invalid token"));

    verify(jwtUtil, times(1)).extractEmail("mock-token");
    verify(bookingService, never()).updateBookingStatus(anyString(), any(BookingStatus.class));
  }

  @Test
  void cancelBooking_success() throws Exception {
    Booking booking = new Booking();
    booking.setStatus(BookingStatus.CANCELLED);
    when(bookingService.updateBookingStatus("booking123", BookingStatus.CANCELLED)).thenReturn(booking);

    mockMvc.perform(put("/api/bookings/booking123/cancel")
                    .header("Authorization", TOKEN)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));

    verify(jwtUtil, times(1)).extractEmail("mock-token");
    verify(bookingService, times(1)).updateBookingStatus("booking123", BookingStatus.CANCELLED);
  }

  @Test
  void deleteBooking_success() throws Exception {
    mockMvc.perform(delete("/api/bookings/booking123")
                    .header("Authorization", TOKEN)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Booking deleted successfully"));

    verify(jwtUtil, times(1)).extractEmail("mock-token");
    verify(bookingService, times(1)).deleteBooking("booking123", USER_EMAIL);
  }

  @Test
  void deleteBooking_invalidToken() throws Exception {
    when(jwtUtil.extractEmail("mock-token")).thenThrow(new RuntimeException("Invalid token"));

    mockMvc.perform(delete("/api/bookings/booking123")
                    .header("Authorization", TOKEN)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid token: Invalid token"));

    verify(jwtUtil, times(1)).extractEmail("mock-token");
    verify(bookingService, never()).deleteBooking(anyString(), anyString());
  }
}
