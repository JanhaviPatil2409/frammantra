package com.farmmantra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.farmmantra.entity.Scheme;

public interface SchemeRepository extends JpaRepository<Scheme, Long> {
}