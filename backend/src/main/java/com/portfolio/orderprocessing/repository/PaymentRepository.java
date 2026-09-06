package com.portfolio.orderprocessing.repository;

import com.portfolio.orderprocessing.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
