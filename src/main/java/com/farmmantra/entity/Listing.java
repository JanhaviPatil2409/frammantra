package com.farmmantra.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "listing")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sellerId;        // farmer.id of the person who posted it

    private String cropName;

    private double quantityKg;

    private double pricePerKg;

    private String location;

    @Column(length = 500)
    private String description;

    private String status;        // AVAILABLE or SOLD

    private LocalDateTime createdAt;
}