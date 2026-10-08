package com.farmmantra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.farmmantra.entity.Farmer;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {
    Optional<Farmer> findByPhone(String phone);
}