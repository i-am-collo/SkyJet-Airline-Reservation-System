package com.skyjet.backend.repository;

import com.skyjet.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBooking_BookingId(Long bookingId);

    List<Transaction> findByUser_UserId(Long userId);
}
