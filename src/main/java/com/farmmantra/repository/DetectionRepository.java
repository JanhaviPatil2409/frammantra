package com.farmmantra.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.farmmantra.entity.Detection;

public interface DetectionRepository extends JpaRepository<Detection, Long> {

    List<Detection> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);
}