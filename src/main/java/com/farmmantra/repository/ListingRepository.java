package com.farmmantra.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.farmmantra.entity.Listing;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByStatusOrderByCreatedAtDesc(String status);

    List<Listing> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}