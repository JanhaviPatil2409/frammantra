package com.farmmantra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.farmmantra.entity.Crop;

public interface CropRepository extends JpaRepository<Crop, Long> {
}