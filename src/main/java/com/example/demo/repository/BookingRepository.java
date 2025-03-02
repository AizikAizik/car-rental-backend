package com.example.demo.repository;

import com.example.demo.model.Booking;
import com.example.demo.model.BookingStatus;
import com.example.demo.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {
  // ✅ Find all bookings by a specific user
  List<Booking> findByUser(User user);

  // ✅ Find all bookings by status (e.g., PENDING, CONFIRMED, CANCELLED)
  List<Booking> findByStatus(BookingStatus status);

  // ✅ Find bookings that have expired (endDate is before current date)
  List<Booking> findByEndDateBefore(Date date);

  // ✅ Find bookings that were created within a specific date range
  List<Booking> findByStartDateBetween(Date startDate, Date endDate);
}
